from langchain_ollama import ChatOllama
from langgraph.prebuilt import create_react_agent
from langchain.tools import tool
import subprocess
import os


llm = ChatOllama(model='PetrosStav/gemma3-tools:4b')


APPS = {
    'tmodloader': r'C:\Users\Oat\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Steam\tModLoader.url'
}


@tool
def open_app_by_name(name: str)->str:
    '''Функция для открытия приложения по названию.'''
    app_path = APPS.get(name.lower())
    if not app_path:
        return 'Приложение не найдено'
    else:
        if app_path.endswith('.exe'):
            try:
                subprocess.Popen(app_path)
                return 'Приложение успешно открыто!'
            except:
                return 'Ошибка при открытии приложения'
        if app_path.endswith('.url'):
            try:
                os.startfile(app_path)
                return 'Приложение успешно открыто!'
            except:
                return 'Ошибка при открытии приложения'



agent = create_react_agent(
    model=llm,
    tools=[open_app_by_name],
    prompt=f'Ты умный помощник с функцией открытия приложений. Вот список имён доступных приложений: {list(APPS.keys())}')
    

print(agent.invoke({'messages': [{'role': 'user', 'content': 'Открой мне tModLoader'}]}))