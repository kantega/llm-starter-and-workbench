# Welcome

Welcome to the Large Language Model (LLM) workbench! The workbench allows playing around interactively, with all parts of a RAG-based chatbot.

If you've come this far, you likely have access to LLMs in one of several ways:
- run locally, e.g. with [Ollama](https://ollama.com/)
- accessed through a service provider, e.g. [huggingface](https://huggingface.co/) or [openai](https://platform.openai.com/)

This will allow you to instantiate one or more LLMs in the workbench, and
start using it in [a simple chat](../tutorials/simple-chat.md)!

But first, some words about the workbench.

## The workbench

The workbench provides a set of *view* (see [list](views.md)) that allow you to explore various elements of Langchain4J and related libraries. The views are organised in *tab groups*. Initially, only some view are shown, but more may be opened from the **View** menu. The tabs may be dragged and docked so you can get a layout suitable for your task (see [demo](https://raw.githubusercontent.com/panemu/tiwulfx-dock/main/media/tiwulfx-dock-demo.gif) at the underlying [tiwulfx-dock library site](https://github.com/panemu/tiwulfx-dock)).

Views may be *linked* so one may provide data to another, e.g.
the selected `StreamingChatLanguageModel` in the [Streaming chat model](no.kantega.llm.fx.StreamingChatLanguageModelsView:/views/no.kantega.llm.fx.StreamingChatLanguageModelsView.md) view may be used by the [Simple chat view](no.kantega.llm.fx.SimpleChatView.md), by linking them together.
When a view is opened, it is automatically linked to other views that provide data it needs, but you can link or unlink manually, if needed (see [linking views](views/linking-views.md)).

Most views have an info page, right-click on the tab and select the **Info** menu item to check it out.

The info pages (like this one) have some limitation that should be noted. Links to http(s) pages are shown in a JavaFX WebView and may not be fully functional. However, the link is copied to the clipboard, so you can easily switch to a real browser and open it there. Hopefully, opening directly in an external browser will soon be supported.

## Getting started

A good place to start is try out a simple chat. This will introduce some basic concepts, such as `ChatLanguageModel`, `ChatMessage` and `ChatMemory` and the use of views for instantiating and selecting LLMs, chatting with the selected LLM and showing the message history.

Go to [Simple chat](../tutorials/simple-chat.md).

After this, you may explore *embeddings*, which are important within LLMs and is the basis for Retrieval Augmented Generation.

Go to [Embeddings](../tutorials/embeddings.md).
