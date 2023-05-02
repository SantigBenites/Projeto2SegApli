import threading, time
from MITMConnection import *

def BankThread(lock:threading.Lock,forwardQueue:list[str],backQueue:list[str],BankSocket):

    while True:

        with lock:
            print("Bank send")
            if len(forwardQueue) != 0:
                message = forwardQueue.pop()
                bankSend(BankSocket,message)
        
        with lock:
            print("Bank receive")
            responseMessage = bankReceive(BankSocket)
            backQueue.append(responseMessage)



def ClientThread(lock:threading.Lock,forwardQueue:list[str],backQueue:list[str],ClientSocket):

    while True:

        with lock:
            message = clientReceive(ClientSocket)
            print(f"Client receive {message}")
            forwardQueue.append(message)

        time.sleep(1)

        with lock:
            if len(backQueue) != 0:
                responseMessage = backQueue.pop(responseMessage)
                print("Client send {responseMessage}")
                clientSend(ClientSocket,responseMessage)
    


