from langchain.tools import tool
import subprocess
import os


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