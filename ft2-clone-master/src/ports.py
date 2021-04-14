import sys
import glob
import serial

ok = 0        # Stores how many ports are available
result = []   # Stores the ports that are available

def outputPorts():
    global ok
    f = open("portList.txt", "w+")  # Open/create file
    f.write(str(ok) + "\n")

    for puerto in result:
        f.write(puerto + ",\n")

    f.close()   # Close file

    print("Ports done!")

    
# Thanks stackoverflow once more :)
def serial_ports():
    global ok
    # Name preparation
    if sys.platform.startswith('win'):
        ports = ['COM%s' % (i + 1) for i in range(256)]
    elif sys.platform.startswith('linux') or sys.platform.startswith('cygwin'):
        # this excludes your current terminal "/dev/tty"
        ports = glob.glob('/dev/tty[A-Za-z]*')
    elif sys.platform.startswith('darwin'):
        ports = glob.glob('/dev/tty.*')
    else:
        raise EnvironmentError('Unsupported platform')

    # Port test and storage
    for port in ports:
        try:
            s = serial.Serial(port)
            s.close()
            result.append(port)
            ok += 1
        except (OSError, serial.SerialException):
            pass
    outputPorts()

serial_ports()