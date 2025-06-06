from langchain_ollama import ChatOllama
from langgraph.prebuilt import create_react_agent
from agent.tools.tools import open_app_by_name



llm = ChatOllama(model='PetrosStav/gemma3-tools:4b')


agent = create_react_agent(
    model=llm,
    tools=[open_app_by_name],
    prompt=f'Ты умный помощник')