#Не рабочая система получения содержимого экрана

#from langchain_ollama import ChatOllama
#from langgraph.prebuilt import create_react_agent
#from tools.tools import *
#from langchain_google_genai import ChatGoogleGenerativeAI
#from langchain_core.messages import HumanMessage
#from secret import api_key
#
##llm = ChatOllama(model='PetrosStav/gemma3-tools:4b')
#llm = ChatGoogleGenerativeAI(
#    api_key=api_key,
#    model="gemini-2.5-flash"
#    )
#
#
#agent = create_react_agent(
#    model=llm,
#    tools=[get_screenshot])
#
#
#def request_to_agent(message: str)->str:
#    messages = [{"role": "system", "content": "Ты умный ии помощник. Не используй markdown или любое другое форматирование текста при ответах."}]
#    screenshot = get_screenshot()
#    mime_type = screenshot["mime_type"]
#    screenshot_data = screenshot["screenshot_data"]
#    messages.append(HumanMessage(content=[
#        {"type": "image_url", "image_url": {"url": f"data:{mime_type};base64, {screenshot_data}"}}
#    ]))
#    messages.append(HumanMessage(content=message))
#
#    return agent.invoke({'messages': messages})['messages'][-1].content
#
#
#print(request_to_agent("Что у меня на экране?"))