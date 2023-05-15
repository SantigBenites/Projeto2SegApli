# Projeto2SegApli

2022/2023

Segurança Aplicada

TP21-G04:
- Santiago Benites fc54392
- Inês Morais fc54462
- Miguel Carvalho fc54399


# Running Attacks

## Replay Attack Group 1

We are able to see the value in account 5000 is 369 and not 236

MITM/Grupo1Replay

Terminal 1
``` python
python MITMG1Replay.py
```

Grupo1/Executables

Terminal 2
```
java -jar Bank.jar
```

Terminal 3
``` java
java -jar MBeC.jar -a 5000 -n 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -a 5000 -d 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -a 5000 -u 5000.user -g
```

## Crash Store Group 1

We are able to see the Store shutdown

Grupo1/Executables

Terminal 1
```
java -jar Bank.jar
```

Terminal 2
```
java -jar Store.jar
```

Terminal 3
``` java
java -jar MBeC.jar -a 5000 -n 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -u 5000.user -a 5000 -c 100.00
java -jar MBeC.jar -u 5000.user -a 5000 -c 100.00
```

## Timing Attack

We should be able to see the time each connection took, which would allow some inference about the type of operation

MITM/General

Terminal 1
```
python MITM.py
```

Grupo1/Executables

Terminal 2
```
java -jar Bank.jar
```

Terminal 3
``` java
java -jar MBeC.jar -a 5000 -n 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -a 5000 -d 123.00 -u 5000.user -p 4000
java -jar MBeC.jar -u 5000.user -a 5000 -c 100.00 -p 4000
```