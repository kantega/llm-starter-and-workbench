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
    - Register with an LLM service provider, like [huggingface](https://huggingface.co/) or [openai](https://platform.openai.com/).
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

[Views may be *linked*](app/src/main/resources/markdown/linking-views.md) so one may provide data to another, e.g.
the selected *embedding model* in the **Embedding models** view may be used by the **Embeddings score** view, by linking them together.
When a view is opened, it is automatically linked to other views that provide data it needs, but you can link or unlink manually, if needed (see below).

Most views have an info page, right-click on the tab and select the **Info** menu item to check it out.

The workbench is a [JavaFX](https://openjfx.io/) application built on [Quarkus](https://quarkus.io/).
Built it with `mvn install` and run with `mvn quarkus:dev -f app`.

## Documentation

Documentation is provided inside the application, using a markdown view.
The markdown files are mainly in the `app` module, in the `src/main/resources/markdown` folder, but some are in other modules.

Diagram are made with [plantuml](https://plantuml.com) and generated using `mvn plantuml:generate -f app`
