from langchain_ollama import ChatOllama
from langgraph.prebuilt import create_react_agent
from tools.tools import open_app_by_name


llm = ChatOllama(model='PetrosStav/gemma3-tools:4b')


agent = create_react_agent(
    model=llm,
    tools=[],
    prompt=f'Ты умный помощник. Отечай только на том языке, на котором с тобой разговаривает пользователь')


def request_to_agent(request: str)->str:
    return agent.invoke({'messages': [{'role': 'user', 'content': request}]})['messages'][-1].content