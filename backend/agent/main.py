from .agent_v2 import app
from langchain_core.messages import HumanMessage


#request to agent without chat history
def request_to_agent(message: str)->str:
    inputs = {"messages": [HumanMessage(content=message)]}
    output = app.invoke(input=inputs)
    return output["messages"][-1].content


#request to agent with chat history
def request_to_agent_chat(messages: list)->str:
    inputs = {"messages": messages}
    output = app.invoke(input=inputs)
    return output["messages"][-1].content