# JBang scripts

[JBang](https://www.jbang.dev/) allows creating and running fairly complex Java programs, without needing to setup a full project with a build system.
By adding build-oriented comments to ordinary Java classes, you can quickly pull in necessary dependencies and get started with minimal configuration hazzle.

## [MinimalChat.java](jbang/MinimalChat.java)

The `ChatLanguageModel` interface provides the `generate` method for responding to a user message.
Is is implemented by OllamaChatModel, an instance of which you can get using a *builder*:

```
OllamaChatModel.builder()
    .baseUrl("http://localhost:11434/")
    .modelName("mistral")
    .build();
```

Here it is assumed you have an Ollama service running locally on (the default) port `11434` and have pulled the `mistral` model.

The chat interface is implemented by a loop that
- reads the next line from the console
- generates a response from the `ChatLanguageModel`
- prints the response

## [ChatWithMemory.java](jbang/ChatWithMemory.java)

The minimal chat has no memory, e.g. if you try asking for a joke and then its explanation with 'tell me a joke' and 'please explain it', the LLM won't know what 'it' refers to.

A `ChatMemory` holds sequence of `ChatMessage` instances, typically an initial `SystemMessage`
followed by alternating `UserMessage` (user input) and `AiMessage` (LLM response) instances. We create a memory
that can hold a maximum of 20 messages with

```
MessageWindowChatMemory.builder()
    .maxMessages(20)
    .id("default")
    .build();
```

During the chat, we call the generate method that takes a message sequence as argument, and take care to add the user's input and the LLM's response in the chat loop,
so the LLM gets a complete (at least the 20 last messages) and correct representation of the dialog.

Test it by again by asking for a joke and then its explanation!

## [ChatWithPrompt.java](jbang/ChatWithPrompt.java)

A `ChatLanguageModel` implicitly provides a suitable response in a dialog, but you can also give more precise instruction and additional contextual information,
as part of the user message. The latter is particularly useful, since an LLM does not have up-to-date information, even not the current date.
E.g try asking the previous chatbot a question about 'today'. However, we can provide the current date as part of the user message.

A `PromptTemplate` is useful for constructing a user message, as a combination of instructions, the user's input and contextual information:

```
PromptTemplate.from("""
    Below is a user message. First, sumnmarize it, and then provide the response.
    Finally, output a random fact of this day of year, given today's date is {{current_date}}.
    User message: "{{user_message}}"
    """)
```

A template includes a set of variables or placeholders, that can be filled in using the `apply` method. Certain placeholders are pre-defined and filled in automatically,
but most must be provided in a map:

```
promptTemplate.apply(Map.of(
    "user_message", text
)).toUserMessage()
```

## [StreamingChat.java](jbang/StreamingChat.java)

An LLM uses some time to generate a full reponse. To give a better interactive user experience, we can utilize the fact the responses are in fact generated incrementally as a stream of 'tokens'. By using a `StreamingChatLanguageModel` and providing a `StreamingResponseHandler` to the `generate` method, we can show the response token by token instead of waiting for the complete response:

```
llm.generate(chatMemory.messages(), new StreamingResponseHandler<AiMessage>() {
    public void onNext(String token) {
        System.out.print(token);
    }
    public void onComplete(Response<AiMessage> response) {
        // call our own callback on the aiMessage representing the complete response
        aiMessageHandler.apply(response.content());
        // print the prompt for the user, to indicate it's his/her turn
        System.out.print("\nuser message to llm > ");
    }
    public void onError(Throwable error) {
        System.out.println("\n... oops, something went wrong!");
    }
});
```

The `StreamingResponseHandler` interface declares three callback methods, `onNext` for the next generated token, `onComplete` for the complete response and `onError` in case of failure.

## [AiServiceChat.java](jbang/AiServiceChat.java)

A chatbot is typically implemented by a complex configuration of collaborating objects, each playing a particular role by implementing a corresponding interface.
Certain configuration patterns can more easily be setup by using an `AiService` 'builder'. It provides a lot of default logic, so you need to specify less. E.g.
you can choose the `ChatMemory` implementation to use, but the logic for adding the messages during the dialog is provided by default. A nice trick is that the
result of calling AiService.build is an instance of an interface defined by you, so all the complexity is abstracted away behind an interface method you declare.

E.g. given the following interface

```
public interface ChatbotAgent {
    TokenStream respond(String userMessage);
}
```
we can create an implementation with
```
chatbot = AiServices.builder(ChatbotAgent.class)
    .streamingChatLanguageModel(llm)
    .chatMemory(chatMemory)
    .build();
```
and use it by calling `chatbotAgent.respond(line)`.

The resulting `TokenStream` is similar to `StreamingResponseHandler`, but takes three callback functions instead of an interface implementation with three methods:

```
chatbotAgent.respond(line)
    .onNext(token -> System.out.print(token))
    .onComplete(response -> {
        aiMessageHandler.apply(response.content());
        System.out.print("\nuser message to llm > ");
    })
    .onError(error -> System.out.println("\n... oops, something went wrong!"))
    .start();
```

For our simple chatbot, using `AiService` doesn't make a big difference, but for the RAG case that follows it makes more sense.

## [RagChat.java](jbang/RagChat.java)

The technique of Retrieval Augmented Generation (RAG) tries to solve the problem that LLMs lack *up-to-date* and *relevant* information for most domains.
It's impractical to use machine learning to teach an LLM new facts, so instead RAG tries to provide all relevant facts as part of user messages,
a bit like we above provided today's date. The relevant facts to include are found using information retrieval (IR) techniques.

Although IR can be done in many ways, using so-called 'embeddings' have become the standard one in the context of LLMs.
An embedding is a *vector of numbers* derived from a sequence of words (phrases, sentences, paragraphs) that in some sense captures the 'meaning' of the words.
By using some metric of similarity of vectors, you can find how 'close' in meaning or topic the corresponding sequence of words are.

A simplified RAG technique has the following steps:
- Gather the documents that contain information/facts that should inform the chatbot. A document can be anything from which you can generate text segments suitable for computing embeddings.
- Generate text segments from the documents, compute corresponding embeddings and store them in an index for fast search and retrieval.
- For each user input in the dialog, search for corresponding text fragments and include the most relevant ones in the user message provided to the LLM.

Langchain4J has tools for all three steps, e.g. `FileSystemDocumentLoader` for loading files, `DocumentSplitters` for generating `TextSegment`s, several implementations of `EmbeddingModel` and `EmbeddingStore` for computing and storing embeddings, and `EmbeddingStoreContentRetriever` for retrieving relevant segments during the dialog.
The API is flexible, it's usually easy to provide custom logic for certain steps, e.g. the script uses [JSoup](https://jsoup.org/) and
the [CopyDown html-to-markdown converter](https://github.com/furstenheim/copy-down) for parsing and transforming HTML files.

To run this script, you need to prepare HTML files of your own and point the `documentsPath` variable to their location.

## [QuarkusRagChat.java](jbang/QuarkusRagChat.java)

This variant of RAG-based chatbot uses Quarkus and its dependency injection (DI) implementation (called ARC) as application platform.
[LlmServices.java](jbang/LlmServices.java) and [application.properties](jbang/application.properties) provides instances (beans) and properties
that are injected into chatbot application.
