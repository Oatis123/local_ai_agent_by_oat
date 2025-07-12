from .agent_v2 import app_gemini, app_gemma, app_qwen
from langchain_core.messages import HumanMessage


#request to gemini agent without chat history
def request_to_gemini_agent(message: str)->str:
    inputs = {"messages": [HumanMessage(content=message)]}
    output = app_gemini.invoke(input=inputs)
    return output["messages"][-1].content

#request to gemma agent without chat history
def request_to_gemma_agent(message: str)->str:
    inputs = {"messages": [HumanMessage(content=message)]}
    output = app_gemma.invoke(input=inputs)
    return output["messages"][-1].content

#request to qwen agent without chat history
def request_to_qwen_agent(message: str)->str:
    inputs = {"messages": [HumanMessage(content=message)]}
    output = app_qwen.invoke(input=inputs)
    return output["messages"][-1].content



#request to gemini agent with chat history
def request_to_gemini_agent_chat(messages: list)->str:
    inputs = {"messages": messages}
    output = app_gemini.invoke(input=inputs)
    return output["messages"][-1].content

#request to gemma agent with chat history
def request_to_gemma_agent_chat(messages: list)->str:
    inputs = {"messages": messages}
    output = app_gemma.invoke(input=inputs)
    return output["messages"][-1].content

#request to qwen agent with chat history
def request_to_qwen_agent_chat(messages: list)->str:
    inputs = {"messages": messages}
    output = app_qwen.invoke(input=inputs)
    return output["messages"][-1].content