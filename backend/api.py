from fastapi import FastAPI
from .agent import request_to_agent_chat
import uvicorn
import json
import uuid
import os 


app = FastAPI()
chats_dir = "backend/chats"


@app.put("/new_message")
def new_message(chat_id: str, new_msg: str):
    file_path = f"{chats_dir}/{chat_id}.json"

    with open(file_path, "r", encoding="utf-8") as f:
        chat_data = json.load(f)
    
    print(chat_data)
    chat_data["messages"].append({
        "role": "user",
        "content": new_msg
        })
    
    agent_response = request_to_agent_chat(chat_data["messages"])

    chat_data["messages"].append({
    "role": "ai",
    "content": agent_response
    })

    return agent_response


#@app.get("/chat_by_id")
#def chat_by_id(chat_id: str):
#    file_path = f"{chats_dir}/{chat_id}.json"
#
#    with open(file_path, "r", encoding="utf-8") as f:
#        chat_data = json.load(f)
#    
#    return chat_data


#@app.get("/new_chat")
#def new_chat():
#    new_chat_id = uuid.uuid4()
#    
#    file_path = f"{chats_dir}/{new_chat_id}.json"
#
#    initial_chat_data = {"messages": []}
#    json_string = json.dumps(initial_chat_data, indent=4, ensure_ascii=False)
#
#    with open(file_path, "w", encoding="utf-8") as f:
#        f.write(json_string)
#        
#    return str(new_chat_id)


if __name__ == "__main__":
    uvicorn.run(app)