from fastapi import FastAPI
import datetime
from .agent.main import *
from pathlib import Path
import uvicorn
import json
import uuid
import os 


app = FastAPI()
chats_dir = "backend/chats"


@app.put("/new_message")
def new_message(chat_id: str, model_name: str, new_msg: str):
    file_path = f"{chats_dir}/{chat_id}.json"

    with open(file_path, "r", encoding="utf-8") as f:
        chat_data = json.load(f)
    
    print(chat_data)
    chat_data["messages"].append({
        "role": "user",
        "content": new_msg
        })
    if model_name == "gemini-2.5-flash":
        agent_response = request_to_gemini_agent_chat(chat_data["messages"])
    elif model_name == "gemma3:4b":
        agent_response = request_to_gemma_agent_chat(chat_data["messages"])
    else:
        agent_response = request_to_qwen_agent_chat(chat_data["messages"])  

    chat_data["messages"].append({
    "role": "ai",
    "content": agent_response
    })

    json_string = json.dumps(chat_data, indent=4, ensure_ascii=False)
    with open(file_path, "w", encoding="utf-8") as f:
        f.write(json_string)

    return agent_response


@app.get("/chat_by_id")
def chat_by_id(chat_id: str):
    file_path = f"{chats_dir}/{chat_id}.json"

    with open(file_path, "r", encoding="utf-8") as f:
        chat_data = json.load(f)
    
    chat = {"messages": chat_data["messages"]}
    print("---------CHAT BY ID----------")
    print(chat)
    return chat


@app.put("/new_chat")
def new_chat(model_name: str, new_message: str):
    new_chat_id = uuid.uuid4()
    
    file_path = f"{chats_dir}/{new_chat_id}.json"

    messsages = [{"role": "user", "content": new_message}]

    #name_prompt = f"Придумай название для чата по первому сообщению от пользователя, напиши только название для чата без своих комментариев: {new_message}"
    name = new_message[:25] + "..."
    if model_name == "gemini-2.5-flash":
        agent_response = request_to_gemini_agent_chat(messsages)
        #name = request_to_gemini_agent(name_prompt)
    elif model_name == "gemma3:4b":
        agent_response = request_to_gemma_agent_chat(messsages)
        #name = request_to_gemma_agent(name_prompt)
    else:
        agent_response = request_to_qwen_agent_chat(messsages)  
        #name = request_to_qwen_agent(name_prompt)

    messsages.append({
        "role": "ai",
        "content": agent_response
        })

    initial_chat_data = {"name": name, "messages": messsages, "date": str(datetime.datetime.now())}
    json_string = json.dumps(initial_chat_data, indent=4, ensure_ascii=False)

    with open(file_path, "w", encoding="utf-8") as f:
        f.write(json_string)
        
    return {"id": new_chat_id, "agent_response": agent_response}


@app.get("/all_chats")
def all_chats():
    directory_path = Path(chats_dir)
    file_names = [item.name for item in directory_path.iterdir()]

    chats = []

    for name in file_names:
        chat_id = name[:-5]
        with open(f"{chats_dir}/{name}", "r", encoding="utf-8") as f:
            chat_data = json.load(f)
        chat_name = chat_data["name"]
        chat_date = chat_data["date"]
        chats.append({"name": chat_name, "id": chat_id, "date": chat_date})
    
    chats.sort(key=lambda x: x["date"])
    chats.reverse()
    sorted_chats = []
    for chat in chats:
        sorted_chats.append({"name": chat["name"], "id": chat["id"]})
    print("----------ALL CHATS----------")
    print(sorted_chats)
    return sorted_chats


@app.get("/delet_chat_by_id")
def delet_chat(id: str):
    try:
        os.remove(f"{chats_dir}/{id}.json")
        return "succesfull"
    except:
        return "error"


if __name__ == "__main__":
    uvicorn.run(app)