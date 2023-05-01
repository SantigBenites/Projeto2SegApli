def stringToArgs(argsString:str):

    if len(argsString) > 4096:
        return 130

    sanitized = argsString.replace(" ","")
    flagBool = False
    args = []

    if sanitized == "":
        return args
    
    if sanitized[0] != "-":
        return 130

    for char in sanitized:

        if flagBool:
            args[-1] += char
            flagBool = False
            args.append("")
        else:
            if char == '-':
                flagBool = True
                args.append("")
                args[-1] += char
            else:
                args[-1] += char

    if len(args) != len(set(args)):
        return 130

    return args