from MITMConnection import *
import queue, time

def BankThread(forwardQueue:queue.Queue(),backQueue:queue.Queue(),BankSocket:socket.socket):

    BankSocket.setblocking(0)
    while True:

        if not forwardQueue.empty():
            message = forwardQueue.get()
            print(f"MITM -> Bank {message}")
            bankSend(BankSocket,message)
    
        try:
            responseMessage = bankReceive(BankSocket)
            if len(responseMessage): 
                #backQueue.clear()
                backQueue.put(responseMessage)
                #print(f"Bank -> MITM {responseMessage[:10]}")
        except Exception:
            continue



def ClientThread(forwardQueue:queue.Queue(),backQueue:queue.Queue(),ClientSocket:socket.socket):
    ClientSocket.setblocking(0)
    while True:

        if not backQueue.empty():
            responseMessage = backQueue.get()
            print(f"Connection with message {responseMessage[:10]} and time {time.time() - start_time}")
            #print(f"MITM -> Client {responseMessage}")
            clientSend(ClientSocket,responseMessage)

        try:
            message = clientReceive(ClientSocket)
            if len(message): 
                #forwardQueue.clear()
                start_time = time.time()
                forwardQueue.put(message)
                #print(f"Client -> MITM {message[:10]}")
        except Exception:
            continue
    


