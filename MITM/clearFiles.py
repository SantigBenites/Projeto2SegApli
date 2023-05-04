import os

dir = f"{os.getcwd()}/Grupo1/Executables"
jarFiles = ["Bank.jar","MBeC.jar","Store.jar"]
for f in os.listdir(dir):
    if f not in jarFiles:
        os.remove(f"{os.getcwd()}/Grupo1/Executables/{f}")
if os.path.isfile(f"{os.getcwd()}/Grupo1/Executables/bank.auth"):
    os.remove(f"{os.getcwd()}/Grupo1/Executables/bank.auth")