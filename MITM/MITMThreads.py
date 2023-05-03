from MITMConnection import *

def BankThread(forwardQueue:list[str],backQueue:list[str],BankSocket):

    BankSocket.setblocking(0)
    while True:

        if len(forwardQueue) != 0:
            print("Bank send")
            message = forwardQueue.pop()
            bankSend(BankSocket,message)
    
        try:
            responseMessage = bankReceive(BankSocket)
            if len(responseMessage)!= 0: 
                backQueue.append(responseMessage)
                print(f"Bank receive")
        except Exception:
            continue



def ClientThread(forwardQueue:list[str],backQueue:list[str],ClientSocket):

    while True:

        if len(backQueue) != 0:
            responseMessage = backQueue.pop()
            print(f"Client send")
            clientSend(ClientSocket,responseMessage)

        try:
            message = clientReceive(ClientSocket)
            if len(message)!= 0: 
                forwardQueue.append(message)
                print(f"Client receive")
        except Exception:
            continue
    


