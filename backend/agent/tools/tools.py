from langchain.tools import tool
import subprocess
import os
import pyautogui
import mss
import io
import base64
from PIL import ImageGrab

#мейби можно добавить автоматизированное получение приложений из реестра
APPS = {
    'tmodloader': r'C:\Users\Oat\AppData\Roaming\Microsoft\Windows\Start Menu\Programs\Steam\tModLoader.url'
}

pyautogui.FAILSAFE = False

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


@tool
def left_click(x: int, y: int)->str:
    '''Функция для нажатия левой кнопкой мыши по нужным координатам'''
    pyautogui.moveTo(x, y, 1)
    pyautogui.leftClick()
    return f"Вы успешно нажали левой кнопкой мыши по координатм x:{x}, y: {y}"


@tool
def right_click(x: int, y: int)->str:
    '''Функция для нажатия правой кнопкой мыши по нужным координатам'''
    pyautogui.moveTo(x, y, 1)
    pyautogui.rightClick()
    return f"Вы успешно нажали правой кнопкой мыши по координатм x:{x}, y: {y}"


@tool
def double_click(x: int, y: int)->str:
    '''Функция для даблклика левой клавишей мыши по нужным координатам'''
    pyautogui.moveTo(x, y, 1)
    pyautogui.doubleClick()
    return f"Вы успешно даблкликнули левойй кнопкой мыши по координатм x:{x}, y: {y}"


#def get_screenshot():
#    '''
#    Функция делает скриншот основного монитора и возвращает его в формате Base64 (JPEG) и его MIME-тип.
#    Используйте этот инструмент, когда нужно увидеть текущее содержимое экрана для анализа или ответа на вопросы о нем.
#    '''
#    try:
#        with mss.mss() as sct:
#            monitors = sct.monitors
#            
#            if len(monitors) < 2:
#                print("WARNING: Меньше двух мониторов обнаружено. Не удалось захватить monitor[1].")
#                return None, None
#
#            monitor_to_capture = monitors[1]
#            sct_img = sct.grab(monitor_to_capture)
#            
#            if sct_img is None or not sct_img.size[0] > 0:
#                print("ERROR: Захват изображения вернул пустой или некорректный объект.")
#                return None, None
#
#            img = Image.frombytes("RGB", sct_img.size, sct_img.rgb)
#            byte_arr = io.BytesIO()
#            img.save(byte_arr, format='JPEG', quality=100)
#            encoded_string = base64.b64encode(byte_arr.getvalue()).decode("utf-8")
#            
#            if not encoded_string:
#                print("ERROR: Закодированная Base64 строка пуста.")
#                return None, None
#
#            return encoded_string, "image/jpeg"
#    except Exception as e:
#        print(f"ERROR: Произошла ошибка при получении скриншота: {e}")
#        return None, None

@tool
def get_screenshot_tool() -> dict:
    """
    Делает скриншот всего экрана и возвращает его в виде строки base64 и MIME-типа.
    Этот инструмент следует использовать, когда нужно увидеть, что в данный момент находится на экране пользователя, чтобы ответить на его вопрос.
    """
    try:
        # Захватываем скриншот
        screenshot = ImageGrab.grab()
        # Сохраняем в байтовый буфер
        from io import BytesIO
        buffered = BytesIO()
        screenshot.save(buffered, format="PNG", quality=100)
        # Кодируем в base64
        img_str = base64.b64encode(buffered.getvalue()).decode("utf-8")
        return {"screenshot_data": img_str, "mime_type": "image/png"}
    except Exception as e:
        return {"error": str(e)}
    

#def get_screenshot() -> dict:
#    """
#    Делает скриншот всего экрана и возвращает его в виде строки base64 и MIME-типа.
#    Этот инструмент следует использовать, когда нужно увидеть, что в данный момент находится на экране пользователя, чтобы ответить на его вопрос.
#    """
#    try:
#        # Захватываем скриншот
#        screenshot = ImageGrab.grab()
#        # Сохраняем в байтовый буфер
#        from io import BytesIO
#        buffered = BytesIO()
#        screenshot.save(buffered, format="PNG", quality=100)
#        # Кодируем в base64
#        img_str = base64.b64encode(buffered.getvalue()).decode("utf-8")
#        return {"screenshot_data": img_str, "mime_type": "image/png"}
#    except Exception as e:
#        return {"error": str(e)}