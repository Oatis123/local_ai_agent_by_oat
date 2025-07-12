import operator
from typing import Annotated, List, TypedDict
from langchain_core.messages import BaseMessage, ToolMessage, HumanMessage
from langchain_google_genai import ChatGoogleGenerativeAI
from langchain_ollama import ChatOllama
from langgraph.graph import StateGraph, END
from .tools.tools import *
from .secret import api_key
import json

class AgentState(TypedDict):
    messages: Annotated[List[BaseMessage], operator.add]


llm_tools_vision = [get_screenshot_tool]
llm_tools = []


gemini = ChatGoogleGenerativeAI(model="gemini-2.5-flash", api_key=api_key).bind_tools(llm_tools_vision)
gemma = ChatOllama(model="PetrosStav/gemma3-tools:4b").bind_tools(llm_tools_vision)
qwen = ChatOllama(model="qwen3:4b").bind_tools(llm_tools)

#Call functions for different models
def call_model_gemini(state: AgentState):
    messages = state["messages"]
    response = gemini.invoke(messages)
    return {"messages": [response]}

def call_model_gemma(state: AgentState):
    messages = state["messages"]
    response = gemma.invoke(messages)
    return {"messages": [response]}

def call_model_qwen(state: AgentState):
    messages = state["messages"]
    response = qwen.invoke(messages)
    return {"messages": [response]}


def call_tool_vision(state: AgentState):
    ai_message = state["messages"][-1]
    tool_calls = ai_message.tool_calls
    messages_to_add = []
    tool_map = {t.name: t for t in llm_tools}

    for tool_call in tool_calls:
        tool_name = tool_call["name"]

        if tool_name == "get_screenshot_tool":
            screenshot = get_screenshot_tool.invoke(tool_call["args"])
            mime_type = screenshot["mime_type"]
            screenshot_data = screenshot["screenshot_data"]
            
            human_message_content = [
                {
                    "type": "text",
                    "text": "Вот запрошенный скриншот для анализа."
                },
                {
                    "type": "image_url",
                    "image_url": {"url": f"data:{mime_type};base64,{screenshot_data}"}
                }
            ]
            messages_to_add.append(HumanMessage(content=human_message_content))

            tool_confirmation = json.dumps({"status": "success", "message": "Image provided in a new message."})
            messages_to_add.append(ToolMessage(content=tool_confirmation, tool_call_id=tool_call["id"]))
        
        else:
            tool_to_call = tool_map.get(tool_name)
            if tool_to_call:
                tool_output = tool_to_call.invoke(tool_call["args"])
                tool_output_str = json.dumps(tool_output, ensure_ascii=False)
                messages_to_add.append(ToolMessage(content=tool_output_str, tool_call_id=tool_call["id"]))

    return {"messages": messages_to_add}

def call_tool(state: AgentState):
    ai_message = state["messages"][-1]
    tool_calls = ai_message.tool_calls
    messages_to_add = []
    tool_map = {t.name: t for t in llm_tools}

    for tool_call in tool_calls:
        tool_name = tool_call["name"]
        tool_to_call = tool_map.get(tool_name)
        if tool_to_call:
            tool_output = tool_to_call.invoke(tool_call["args"])
            tool_output_str = json.dumps(tool_output, ensure_ascii=False)
            messages_to_add.append(ToolMessage(content=tool_output_str, tool_call_id=tool_call["id"]))

    return {"messages": messages_to_add}


def should_continue(state: AgentState):
    if state["messages"][-1].tool_calls:
        return "call_tool"
    else:
        return END
    

workflow_gemini = StateGraph(AgentState)
workflow_gemma = StateGraph(AgentState)
workflow_qwen = StateGraph(AgentState)

workflow_gemini.add_node("agent", call_model_gemini)
workflow_gemini.add_node("action", call_tool_vision)

workflow_gemma.add_node("agent", call_model_gemma)
workflow_gemma.add_node("action", call_tool_vision)

workflow_qwen.add_node("agent", call_model_qwen)
workflow_qwen.add_node("action", call_tool)


workflow_gemini.set_entry_point("agent")
workflow_gemma.set_entry_point("agent")
workflow_qwen.set_entry_point("agent")

workflow_gemini.add_conditional_edges(
    "agent",
    should_continue,
    {
        "call_tool": "action",
        END: END
    }
)

workflow_gemma.add_conditional_edges(
    "agent",
    should_continue,
    {
        "call_tool": "action",
        END: END
    }
)

workflow_qwen.add_conditional_edges(
    "agent",
    should_continue,
    {
        "call_tool": "action",
        END: END
    }
)

workflow_gemini.add_edge("action", "agent")
workflow_gemma.add_edge("action", "agent")
workflow_qwen.add_edge("action", "agent")

app_gemini = workflow_gemini.compile()
app_gemma = workflow_gemma.compile()
app_qwen = workflow_qwen.compile()