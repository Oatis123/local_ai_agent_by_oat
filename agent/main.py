from fastapi import FastAPI
from agent import request_to_agent
import uvicorn

app = FastAPI()


@app.get("/v1")
def agent_request(prompt: str):
    print(f"--- FastAPI: Получен prompt: '{prompt}' ---")
    try:
        result = request_to_agent(prompt)
        print(f"--- FastAPI: Результат от request_to_agent: {result} ---")
        return result
    except Exception as e:
        print(f"--- FastAPI: Ошибка при вызове request_to_agent: {e} ---")
        raise e


if __name__ == "__main__":
    uvicorn.run(app)
