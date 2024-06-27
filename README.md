# Getting started with LLMs

This repo is for getting started with large language models (LLMs) using Langchain4J. It consists of two parts

1. a set of JBang 'scripts' that implement variants of pretty minimal chatbots
2. a workbench for trying out various components and techniques supported by Langchain4J, such as document 'ingestion', embeddings, chat language models and RAG

The scripts give a quick introduction to basic concepts as code, e.g. `ChatMessage`, `ChatMemory` and `ChatLanguageModel`. Each script can be run with `jbang <path-to-script>`, e.g. `jbang jbang/MinimalChat.java`.

The workbench allows playing around more interactively, with all parts of a RAG-based chatbot, by means of a number of specialized views.
Build it with `mvn install` and run with `mvn quarkus:dev -f app` (but note the requirements below).

## Requirements

- [Install jbang](https://www.jbang.dev/documentation/guide/latest/installation.html), e.g. using [sdkman](https://sdkman.io/).
    - If you're using VSCode, consider installing the [JBang extensions](https://github.com/jbangdev/jbang-vscode).
- Get access to LLM services, at least one of the following:
    - Install [ollama](https://ollama.com/), so you can run LLM services locally.
    - Register with an LLM service provider, like [openai](https://platform.openai.com/) or [huggingface](https://huggingface.co/).
- Edit the [application.properties](app/src/main/resources/application.properties) file as you see fit and define environment necessary variables or create a `.env` file in the `app` for secrets/keys. See [quarkus' configuration documentation](https://quarkus.io/guides/config#secrets-in-environment-properties) for details.

## JBang scripts

[JBang](https://www.jbang.dev/) allows creating and running fairly complex Java programs, without needing to setup a full project with a build system.
By adding build-oriented comments to ordinary Java classes, you can quickly pull in necessary dependencies and get started with minimal configuration hazzle.

Located in the [jbang folder](jbang/README.md), we have the following jbang-runnable scripts:

- [MinimalChat.java](jbang/MinimalChat.java) - a minimal console chatbot using an `OllamaChatModel` (which implements the `ChatLanguageModel` interface)
- [ChatWithMemory.java](jbang/ChatWithMemory.java) - a chatbot that uses a `ChatMemory` to keep track of the dialog (a sequence of `ChatMessage` alternating between `UserMessage` and `AiMessage`), so you can refer (implicitly or explicitly) to previous messages
- [ChatWithPrompt.java](jbang/ChatWithPrompt.java) - uses a `PromptTemplate` to give instructions for how to react to a user message and provide contextual information (today's date)
- [StreamingChat.java](jbang/StreamingChat.java) - uses a `StreamingChatLanguageModel` so responses are streamed as they are generated
- [AiServiceChat.java](jbang/AiServiceChat.java) - a different way of rigging a chatbot, by means of an `AiService` builder
- [RagChat.java](jbang/RagChat.java) - a chatbot implementing Retrieval Augmented Generation (RAG), by loading documents with`FileSystemDocumentLoader`, segmenting and storing them in an `EmbeddingStore` with an `EmbeddingStoreIngestor` and retrieving relevant segments and providing them as context with a `EmbeddingStoreContentRetriever`
- [QuarkusRagChat.java](jbang/QuarkusRagChat.java) - a chatbot implementing RAG rigged as a quarkus application and utilizating depencency injection

Running `jbang` on any of these will download dependencies, compile the source and run the `main` method.

## LLM Workbench

The workbench provides a set of *views* that allow you to explore various elements of Langchain4J.
The views are organised in *tab groups*, initially, only some view are shown, but more may be opened from the **View** menu, and
the tabs may be dragged and docked so you can get a layout suitable for your task.

Views may be *linked* so one may provide data to another, e.g.
the selected *embedding model* in the **Embedding models** view may be used by the **Embeddings score** view, by linking them together.
When a view is opened, it is automatically linked to other views that provide data it needs, but you can link or unlink manually, if needed (see below).

Most views have an info page, right-click on the tab and select the **Info** menu item to check it out.

The workbench is a [JavaFX](https://openjfx.io/) application built on [Quarkus](https://quarkus.io/).
Built it with `mvn install` and run with `mvn quarkus:dev -f app`.

### Views

The **View** menu has entries for each kind of view, organized by category. Select an entry to create a new view.

* [Embedding models view](app/src/main/resources/markdown/no.kantega.llm.fx.EmbeddingModelsView.md) - lists all available embedding models

* [Chat models view](app/src/main/resources/markdown/no.kantega.llm.fx.ChatLanguageModelView.md) - lists all available chat models

* [Streaming chat models view](app/src/main/resources/markdown/no.kantega.llm.fx.StreamingChatLanguageModelView.md) - lists all available streaming chat models

* [Ollama models view](ollama/src/main/resources/markdown/no.kantega.llm.fx.OllamaModelsView.md) - create new ollama chat models

* [OpenAi models view](openai/src/main/resources/markdown/no.kantega.llm.fx.OpenaiChatModelsView.md) - create new open ai chat models

* [Hugging face models view](huggingface/src/main/resources/markdown/no.kantega.llm.fx.HuggingfaceModelView.md) - create new hugging face chat models

* [Simple chat view](app/src/main/resources/markdown/no.kantega.llm.fx.SimpleChatView.md) - chat with a (streaming) chat model

* [Chat memory view](app/src/main/resources/markdown/no.kantega.llm.fx.ChatMemoryView.md) - shows the messages in a chat memory

* [Embeddings score view](app/src/main/resources/markdown/no.kantega.llm.fx.EmbeddingsScoreView.md) - compute and compare (the similarity of) embeddings

* [Uri documents view](app/src/main/resources/markdown/no.kantega.llm.fx.UriDocumentsView.md) - load documents, to use as input for the **Ingestor** view

* [Ingestor view](app/src/main/resources/markdown/no.kantega.llm.fx.IngestorView.md) - split documents into text segments and compute their embeddings

* [Embeddings search view](app/src/main/resources/markdown/no.kantega.llm.fx.EmbeddingsSearchView.md) - search embeddings for text fragments similar to a sentence

* [Rag chat view](app/src/main/resources/markdown/no.kantega.llm.fx.RagChatView.md) - chat with a chat model, using RAG to provide answers

* [Csv view](app/src/main/resources/markdown/no.kantega.llm.fx.CsvView.md) - load and view csv data

* [Bar chart view](app/src/main/resources/markdown/no.kantega.llm.fx.BarChartView.md) - view table data as bar chart

* [Stacked bar chart view](app/src/main/resources/markdown/no.kantega.llm.fx.StackedBarChartView.md) - view table data as stacked bar chart

* [Expression view](app/src/main/resources/markdown/no.kantega.llm.fx.ExpressionView.md) - evaluate expressions

### Linking views

Views may be linked, to pass data from one to another. E.g. since the **Embeddings score** view *needs* an embedding model,
it may be linked to the (selection in the) **Embedding models** view.

Actually, it's not the views that are linked, but interactive or visual elements, so an interactve element *providing* data may be linked to one *needing* the same kind of data.
In the mentioned case, the list in the **Embedding models** view *provides* an embedding model, and the score button in the **Embeddings score** view *needs* one.

When a view is created, the elements needing data are automatically linked to ones providing the corresponding kind of data. So if an **Embeddings score** view is
created while an **Embedding models** view is open, the appropriate link is also created. You can check this by pressing appropriate modifier keys (ALT+META);
green arrows will show which pairs of elements are linked. (They overlay may be a bit confusing, as arrows will also be shown for hidden elements).

When pressing these modifiers, possible source elements are highlighted in blue, and
if you press and drag with the mouse, and release above a valid target (highlighed in green) you can create a new link.
The remove a link, press the modifier keys and click on the link you want to remove.

In the image below, the chat response text field in two chat views are linked to respective **Chat memory** views.

<img src="readme-files/linked-views.png" alt="Linked view" width="1000"/>
