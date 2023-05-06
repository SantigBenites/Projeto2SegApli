from MITMConnection import *
import queue, time, threading

def BankThread(forwardQueue:queue.Queue(),backQueue:queue.Queue(),BankSocket:socket.socket, packetNum:int, trace:list):

    BankSocket.setblocking(0)
    packetsSent = 0
    startTime = time.time()
    tracing = trace != None
    
    while packetsSent < packetNum and time.time() - startTime < 1.5:

        if not forwardQueue.empty():
            message = forwardQueue.get()
            if tracing:
                trace.append(message)
            bankSend(BankSocket,message)
            #print(f"MITM -> Bank {message[:10]}")
    
        try:
            responseMessage = bankReceive(BankSocket)
            if len(responseMessage): 
                backQueue.put(responseMessage)
                packetsSent = packetsSent + 1
                #print(f"Bank -> MITM {responseMessage[:10]} packets sent {packetsSent}")
        except Exception:
            continue



def ClientThread(forwardQueue:queue.Queue(),backQueue:queue.Queue(),ClientSocket:socket.socket,packetNum:int):

    ClientSocket.setblocking(0)
    packetsSent = 0
    startTime = time.time()

    while packetsSent < packetNum and time.time() - startTime < 1.5:

        if not backQueue.empty():
            responseMessage = backQueue.get()
            clientSend(ClientSocket,responseMessage)
            packetsSent = packetsSent + 1
            #print(f"MITM -> Client {responseMessage[:10]} packets sent {packetsSent}")

        try:
            message = clientReceive(ClientSocket)
            if len(message): 
                forwardQueue.put(message)
                print(f"Client -> MITM {message[:10]}")
        except Exception:
            continue
    


def proxyConnection(ClientSocket:socket.socket,BankSocket:socket.socket,numPackets:int, trace:list=None):

    forwardQueue = queue.Queue()
    backQueue = queue.Queue()

    # BankThread
    bankThread = threading.Thread(target=BankThread, args=(forwardQueue,backQueue,BankSocket,numPackets,trace))
    bankThread.start()

    # ClientThread
    clientThread = threading.Thread(target=ClientThread, args=(forwardQueue,backQueue,ClientSocket,numPackets))
    clientThread.start()

    bankThread.join()
    clientThread.join()

    return trace


def bankSendThread(BankSocket:socket.socket, trace:list):

    BankSocket.setblocking(0)

    for message in trace:
        bankSend(BankSocket,message) 
        print(f"message {message[:10]}")

def bankReceiveThread(BankSocket:socket.socket):

    BankSocket.setblocking(0)

    startTime = time.time()
    while True:
        try:
            bankReceive(BankSocket)
        except Exception:
            continue

def replay(BankSocket:socket.socket, trace:list):

    BankSocket.setblocking(0)

    # ClientThread
    sendTread = threading.Thread(target=bankSendThread, args=(BankSocket,trace))
    sendTread.start()

    # ClientThread
    receiveThread = threading.Thread(target=bankReceiveThread, args=(BankSocket,))
    receiveThread.start()

    sendTread.join()
    #receiveThread.join()

    print("Replay completed")
    return 