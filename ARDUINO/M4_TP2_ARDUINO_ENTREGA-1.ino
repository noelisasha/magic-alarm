/***** ALARM - Codigo con estados - Grupo 4 ****/

///////////////////////////////////////////////////////////////////////////////
/*** Importar Bibliotecas ***/
#include <SoftwareSerial.h>


/*** Constantes - Pines ***/
#define PIN_RELAY                          13
#define PIN_SENSOR_LIGHT                   A5
#define PIN_SENSOR_BUTTON                  2
#define PIN_ACTUATOR_BUZZER                8
#define PIN_ACTUATOR_LAMP                  3
#define PIN_BLUETOOTH_RX                   10
#define PIN_BLUETOOTH_TX                   11

/*** Constantes - Melodia  ***/
#define NOTE_B0  31
#define NOTE_C1  33
#define NOTE_CS1 35
#define NOTE_D1  37
#define NOTE_DS1 39
#define NOTE_E1  41
#define NOTE_F1  44
#define NOTE_FS1 46
#define NOTE_G1  49
#define NOTE_GS1 52
#define NOTE_A1  55
#define NOTE_AS1 58
#define NOTE_B1  62
#define NOTE_C2  65
#define NOTE_CS2 69
#define NOTE_D2  73
#define NOTE_DS2 78
#define NOTE_E2  82
#define NOTE_F2  87
#define NOTE_FS2 93
#define NOTE_G2  98
#define NOTE_GS2 104
#define NOTE_A2  110
#define NOTE_AS2 117
#define NOTE_B2  123
#define NOTE_C3  131
#define NOTE_CS3 139
#define NOTE_D3  147
#define NOTE_DS3 156
#define NOTE_E3  165
#define NOTE_F3  175
#define NOTE_FS3 185
#define NOTE_G3  196
#define NOTE_GS3 208
#define NOTE_A3  220
#define NOTE_AS3 233
#define NOTE_B3  247
#define NOTE_C4  262
#define NOTE_CS4 277
#define NOTE_D4  294
#define NOTE_DS4 311
#define NOTE_E4  330
#define NOTE_F4  349
#define NOTE_FS4 370
#define NOTE_G4  392
#define NOTE_GS4 415
#define NOTE_A4  440
#define NOTE_AS4 466
#define NOTE_B4  494
#define NOTE_C5  523
#define NOTE_CS5 554
#define NOTE_D5  587
#define NOTE_DS5 622
#define NOTE_E5  659
#define NOTE_F5  698
#define NOTE_FS5 740
#define NOTE_G5  784
#define NOTE_GS5 831
#define NOTE_A5  880
#define NOTE_AS5 932
#define NOTE_B5  988
#define NOTE_C6  1047
#define NOTE_CS6 1109
#define NOTE_D6  1175
#define NOTE_DS6 1245
#define NOTE_E6  1319
#define NOTE_F6  1397
#define NOTE_FS6 1480
#define NOTE_G6  1568
#define NOTE_GS6 1661
#define NOTE_A6  1760
#define NOTE_AS6 1865
#define NOTE_B6  1976
#define NOTE_C7  2093
#define NOTE_CS7 2217
#define NOTE_D7  2349
#define NOTE_DS7 2489
#define NOTE_E7  2637
#define NOTE_F7  2794
#define NOTE_FS7 2960
#define NOTE_G7  3136
#define NOTE_GS7 3322
#define NOTE_A7  3520
#define NOTE_AS7 3729
#define NOTE_B7  3951
#define NOTE_C8  4186
#define NOTE_CS8 4435
#define NOTE_D8  4699
#define NOTE_DS8 4978
#define REST     0
#define SERIAL   9600

/*** Constantes -  States ***/
// States del SE
#define ST_ALARM_IDLE             1
#define ST_ALARM_LIGHTING         2
#define ST_ALARM_LIGHTING_MUSIC   3
#define ST_ALARM_OFF              4
/***


/*** Constantes -  Events ***/
#define EVT_TIMEOUT             1000
#define EVT_CONTINUE              2000 // EVT_DUMMY
#define EVT_LIGHT_DETECTED            3000
#define EVT_FINISH_SEQUENCE             5000
#define EVT_BUTTON_PRESSED                6000
#define EVT_UNKNOWN                     7000
#define EVT_START_SEQUENCE                  8000
#define EVT_EXECUTE_SEQUENCE                9000
#define EVT_TURN_OFF_LAMP                   1200
#define EVT_TURN_ON_LAMP                    1300
#define EVT_PLAY_MUSIC                      1400


/*** Constantes -  Otras ***/
#define TIME_MAX_MILIS_TIMEOUT        50
#define TIME_MILIS_ONE_SECOND           1000
#define TIME_MILIS_TWO_SECONDS          2000
#define TIME_MILIS_THREE_SECONDS        3000
#define MAX_LIGHT_THRESHOLD             800
#define ON                              1
#define OFF                             0
#define FIRST_TIME_INDEX                0
#define TEMPO                           1000
#define TEMPO1                          60000
#define TEMPO2                          4
#define CANT_NOTA                       2
#define CERO                            0
#define CANT_REPE                       3
#define INDEX                           -1
#define DUR_NOTA                        1.1
#define DUR_NOTA2                       1.8

/*** DEBUG - Puerto Serial ***/
#define SERIAL_DEBUG_ENABLED 0
#if SERIAL_DEBUG_ENABLED
#define DebugPrint(str)\
    {\
    Serial.println(str);\
    }
#else
  #define DebugPrint(str)
#endif

#define DebugOutput(estado,event)\
      {\
        String est = estado;\
        String evt = event;\
        String str;\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
        str = "State -> [" + est + "]: " + "Event -> [" + evt + "].";\
        DebugPrint(str);\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
      }

///////////////////////////////////////////////////////////////////////////////

/*** Variables Globales ***/
SoftwareSerial BTserial(PIN_BLUETOOTH_RX, PIN_BLUETOOTH_TX); 
int current_state;
bool timeout;
int sequencialLightTimesLength;
int sequencialLightTimes[3];
int sequencialLightTimesIndex;
bool isLightOn;
bool outputTone=false;
unsigned long pauseBetweenNotes;
unsigned long currentMillis2, previous_time2;
bool verify_light_sensor, verify_bluetooth_module;
bool execute_light_sequence;
string string_input_bluetooth = "";

struct stEvent
{
  int type;
  int param1;
};
stEvent event;

struct stLightSensor
{
    int estado;
    long current_value;
    long past_value;
};
stLightSensor lightSensor;

/*** Variables de State ***/
// De Sensores/Actuadores
int button_state;

// Del Temporizador
int current_time; // tiempo_actual
int previous_time; // tiempo_anterior

// Variables para la melodia
// Notas de la melodia junto a duración
    // 4 = a quarter note, 8 = eighteenth note, 16 = sixteenth note...
    // Numeros negativos = dotted notes. Ej.: -4 = a dotted quarter note = a quarter + eighteenth
int melody[] = {

  // Hedwig's theme from the Harry Potter Movies - Socre from https://musescore.com/user/3811306/scores/4906610
  REST, 2, NOTE_D4, 4,
  NOTE_G4, -4, NOTE_AS4, 8, NOTE_A4, 4,
  NOTE_G4, 2, NOTE_D5, 4,
  NOTE_C5, -2, 
  NOTE_A4, -2,
  NOTE_G4, -4, NOTE_AS4, 8, NOTE_A4, 4,
  NOTE_F4, 2, NOTE_GS4, 4,
  NOTE_D4, -1, 
  NOTE_D4, 4,

  NOTE_G4, -4, NOTE_AS4, 8, NOTE_A4, 4, //10
  NOTE_G4, 2, NOTE_D5, 4,
  NOTE_F5, 2, NOTE_E5, 4,
  NOTE_DS5, 2, NOTE_B4, 4,
  NOTE_DS5, -4, NOTE_D5, 8, NOTE_CS5, 4,
  NOTE_CS4, 2, NOTE_B4, 4,
  NOTE_G4, -1,
  NOTE_AS4, 4,
     
  NOTE_D5, 2, NOTE_AS4, 4,//18
  NOTE_D5, 2, NOTE_AS4, 4,
  NOTE_DS5, 2, NOTE_D5, 4,
  NOTE_CS5, 2, NOTE_A4, 4,
  NOTE_AS4, -4, NOTE_D5, 8, NOTE_CS5, 4,
  NOTE_CS4, 2, NOTE_D4, 4,
  NOTE_D5, -1, 
  REST,4, NOTE_AS4,4,  

  NOTE_D5, 2, NOTE_AS4, 4,//26
  NOTE_D5, 2, NOTE_AS4, 4,
  NOTE_F5, 2, NOTE_E5, 4,
  NOTE_DS5, 2, NOTE_B4, 4,
  NOTE_DS5, -4, NOTE_D5, 8, NOTE_CS5, 4,
  NOTE_CS4, 2, NOTE_AS4, 4,
  NOTE_G4, -1, 
  
};
// sizeof --> Obtener nro de bytes, donde: 1 valor 'int' = 2 bytes = 16 bits
// Hay 2 valores por nota (pitch y duracion) ==> Para cada nota = 4 bytes
int notes = sizeof(melody) / sizeof(melody[0]) / CANT_NOTA;
int tempo = TEMPO;
// Calcular la duración (en ms) de una nota completa = (60s/tempo)*4 beats
int wholenote = (TEMPO1 * TEMPO2) / tempo;
int divider = 0, noteDuration = 0;
int thisNote = 0;

///////////////////////////////////////////////////////////////////////////////

// Funciones utilitarias para Transiciones
long readLightSensor() {
  return analogRead(PIN_SENSOR_LIGHT);
}

long readButtonSensor() {
  return digitalRead(PIN_SENSOR_BUTTON);
}

void turnOnRelay() {
    digitalWrite(PIN_RELAY, HIGH);
}

void turnOffRelay() {
    digitalWrite(PIN_RELAY, LOW);
}

void playNote() {
  
    // El array melody tiene tamaño doble que la cant. de  notas (notes + durations)
    bool condition = thisNote < notes * CANT_NOTA;
  currentMillis2 = millis();

    // Calcular la duración de la nota
    divider = melody[thisNote + 1];     
    if (divider > CERO) {
        // proceder si es nota común
        noteDuration = (wholenote) / divider;
    } else if (divider < CERO) {
        // si es nota negativa, es "dotted note"
        noteDuration = (wholenote) / abs(divider);
        noteDuration *= DUR_NOTA2; // incrementa la duración de estas notas por la mitad
    }

  
    if(condition) {
  
        if (outputTone && ((currentMillis2 - previous_time2) >= noteDuration)) {
            previous_time2 = currentMillis2;
            emitSound(false);
            outputTone = false;
            thisNote+=2;
        }
        else {
          pauseBetweenNotes = (noteDuration * DUR_NOTA);

            if ((currentMillis2 - previous_time2) >= pauseBetweenNotes) {
                previous_time2 = currentMillis2;
                Serial.println(melody[thisNote]);
                if (melody[thisNote] == CERO) {
                    emitSound(false);
                } else {
                    emitSound(true);
                }
                outputTone = true;
            }
        } 

    } else {
      thisNote = CERO;
    }
}

void turnOffBuzzer() {
  noTone(PIN_ACTUATOR_BUZZER);
}

void initiateSequence() {
    
    turnOnRelay(); // Se usa el relay para iniciar el proceso de lampara
    event.type = EVT_EXECUTE_SEQUENCE;

}

void finishSequence() {

    turnOnRelay(); //Prendemos la lampara para que quede encendida para el próximo estado
    isLightOn = true;

    execute_light_sequence = false;

    current_state = ST_ALARM_LIGHTING_MUSIC; 
    event.type = EVT_PLAY_MUSIC;
}

void emitSound(bool action) {

    if(action) {

        // Solo tocamos una nota durantel el 90% de la duración, dejando una pausa del 10%
        tone(PIN_ACTUATOR_BUZZER, melody[thisNote]);  

    } else {
        noTone(PIN_ACTUATOR_BUZZER);
    }
  
}


bool verifyLightSensorState( ) {

    lightSensor.current_value = readLightSensor( );
    

    int current_value_reading = lightSensor.current_value;
    Serial.println(current_value_reading);
    int last_value_reading = lightSensor.past_value;

    if(  (current_value_reading > MAX_LIGHT_THRESHOLD) && (current_value_reading != last_value_reading) )  {
        
        lightSensor.past_value = current_value_reading;
        event.type   = EVT_LIGHT_DETECTED;
        event.param1 = current_value_reading;

        return true;

    }

    return false;
}

bool verifyBluetoothCom() {
   //Si se reciben datos desde el módulo de Bluetooth HC05 
    if (BTserial.available()) { 
        //Se leen y muestra, a traves del el monitor serie, los datos recibidos. 
        string_input_bluetooth = BTserial.read();
        Serial.write(string_input_bluetooth);
        return true;
     }

     return false;
}

bool setBluetoothScreen() {
    /**
    //Si se ingresan datos por teclado en el monitor serie  <====================
    if (Serial.available()) {
        //se los lee y se los envia al HC05
        string_input_bluetooth =  Serial.read();
        Serial.write(c);
        BTserial.write(c); 
    }
    **/
}

void turnActuatorsOff() {
    if(event.type == EVT_PLAY_MUSIC) {
        turnOffRelay(); 
        turnOffBuzzer();        
        event.type = EVT_BUTTON_PRESSED;
    }
}

///////////////////////////////////////////////////////////////////////////////
void getEvents() {
    if(verifyBluetoothCom()) {
        
        return;
    }
    if(verify_light_sensor){

        // Verificar Temporizador && Timeout
        current_time = millis(); // Tomar el tiempo actual.

        int  difference = (current_time - previous_time);
        timeout = (difference > TIME_MAX_MILIS_TIMEOUT)? (true):(false); // Verifica cuanto tiempo transcurrió

        if(timeout) {
            
            timeout = false; // limpio variable 
            previous_time = current_time;
            
            // Verificar Sensor de Luz
            if( verifyLightSensorState() ) {
                verify_light_sensor = false;
                execute_light_sequence = true;
                return;
            }
        }        
        
    } else if(execute_light_sequence) {
        Serial.println("entro");
        current_time = millis(); // Tomar el tiempo actual.

        int  difference = (current_time - previous_time);
        timeout = (difference > sequencialLightTimes[sequencialLightTimesIndex])? (true):(false); // Verifica cuanto tiempo 

        if(sequencialLightTimesIndex < FIRST_TIME_INDEX){
            sequencialLightTimesIndex = FIRST_TIME_INDEX;
            isLightOn = true;
            event.type = EVT_TURN_ON_LAMP;
        } else if ( timeout ) {
            // Actualizo el tiempo anterior.
            previous_time = current_time;

            if(isLightOn){
                event.type = EVT_TURN_OFF_LAMP;
                isLightOn = false;
            } else {
                event.type = EVT_TURN_ON_LAMP;
                isLightOn = true;
                if(sequencialLightTimesIndex == sequencialLightTimesLength){
                    event.type = EVT_FINISH_SEQUENCE;
                } else 
                    sequencialLightTimesIndex++;
                
            }   
            return;
        } else 
            // Genero event dummy ....
            event.type = EVT_CONTINUE;
    }
    
}


///////////////////////////////////////////////////////////////////////////////
void initiate() {

    /** Declarar pines de Entrada y/o Salida **/
    Serial.begin(SERIAL);
    BTserial.begin(9600);  //Configurar la velocidad de transferencia de datos entre el Bluethoot HC05 y Android.


    pinMode(PIN_RELAY, OUTPUT); //Relay (activa loop prender luz)
    pinMode(PIN_SENSOR_LIGHT, INPUT);
    pinMode(PIN_SENSOR_BUTTON, INPUT);
    pinMode(PIN_ACTUATOR_BUZZER, OUTPUT);
    pinMode(PIN_ACTUATOR_LAMP, OUTPUT);

    /** Inicializaciones **/
    // Inicializar el Estado inicial
    current_state = ST_ALARM_IDLE;
    event.type = EVT_CONTINUE;

    verify_light_sensor = true;
    execute_light_sequence = false;

    sequencialLightTimesLength = sizeof(sequencialLightTimes) / sizeof(sequencialLightTimes[0]);

    sequencialLightTimesLength = CANT_REPE;
    sequencialLightTimes[0] = TIME_MILIS_ONE_SECOND;
    sequencialLightTimes[1] = TIME_MILIS_TWO_SECONDS;
    sequencialLightTimes[2] = TIME_MILIS_THREE_SECONDS;
    sequencialLightTimesIndex = INDEX;

    thisNote = CERO; //Reinicializar valor de nota actual

    timeout = false;
    current_time = millis(); //Inicializacion de temporizador - // Toma la primera medición del tiempo.
    previous_time = millis(); //Inicializacion de temporizador- // Toma la primera medición del tiempo.
    currentMillis2 = millis(); // Toma la primera medición del tiempo.
    previous_time2 = millis(); // Toma la primera medición del tiempo.

    attachInterrupt(digitalPinToInterrupt(PIN_SENSOR_BUTTON), turnActuatorsOff, RISING); 

}

void alarm_state_machine() {

    getEvents();

    switch(current_state) {

        case ST_ALARM_IDLE:
            {
                switch(event.type) {

                    case EVT_LIGHT_DETECTED:
                        {
                            DebugOutput("ST_ALARM_IDLE", "EVT_LIGHT_DETECTED");             /** DEBUG! **/
                            current_state = ST_ALARM_LIGHTING;              
                        }
                    break;

                    case EVT_CONTINUE:
                        {
                            DebugOutput("ST_ALARM_IDLE", "EVT_CONTINUE");                   /** DEBUG! **/                            
                            current_state = ST_ALARM_IDLE;
                        }
                    break;
                
                    default:
                        {
                            DebugOutput("ST_ALARM_IDLE", "EVT_UNKNOWN");                    /** DEBUG! **/
                        }
                    break;

                }
            }
            break;

        case ST_ALARM_LIGHTING:
            {
                switch(event.type) {
                    case EVT_LIGHT_DETECTED:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_LIGHT_DETECTED");       /** DEBUG! **/
                            initiateSequence();
                            current_state = ST_ALARM_LIGHTING;           
                        }
                    break;

                    case EVT_EXECUTE_SEQUENCE:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_EXECUTE_SEQUENCE");       /** DEBUG! **/
                            current_state = ST_ALARM_LIGHTING;  
                        }
                    break;   

                    case EVT_FINISH_SEQUENCE:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_FINISH_SEQUENCE");       /** DEBUG! **/
                            finishSequence();
                       
                        }
                    break;  

                    case EVT_TURN_OFF_LAMP:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_TURN_OFF_LAMP");       /** DEBUG! **/
                            turnOffRelay();
                            current_state = ST_ALARM_LIGHTING;  
                       
                        }
                    break;    
                    case EVT_TURN_ON_LAMP:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_TURN_ON_LAMP");       /** DEBUG! **/
                            turnOnRelay();
                            current_state = ST_ALARM_LIGHTING;  
                       
                        }
                    break;    

                    case EVT_CONTINUE:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_CONTINUE");                   /** DEBUG! **/
                            current_state = ST_ALARM_LIGHTING;
                        }
                    break;                  

                    default:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_UNKNOWN");                   /** DEBUG! **/
                        }
                    break;

                }
            }
            break;

        case ST_ALARM_LIGHTING_MUSIC:
            {
               
                switch(event.type) {
                    case EVT_PLAY_MUSIC:
                        {
                            DebugOutput("ST_ALARM_LIGHTING_MUSIC", "EVT_PLAY_MUSIC");     /** DEBUG! **/
                            playNote(); 
                            current_state = ST_ALARM_LIGHTING_MUSIC;  
                       
                        }
                    break; 
                    case EVT_BUTTON_PRESSED:
                        {
                            DebugOutput("ST_ALARM_LIGHTING_MUSIC", "EVT_BUTTON_PRESSED");     /** DEBUG! **/
                            current_state = ST_ALARM_OFF;                        
                        }
                    break; 

                    case EVT_CONTINUE:
                    default:
                        {
                            DebugOutput("ST_ALARM_LIGHTING_MUSIC", "EVT_CONTINUE");        /** DEBUG! **/
                            current_state = ST_ALARM_LIGHTING_MUSIC;
                        }
                    break;  

                }
            }
            break;

        case ST_ALARM_OFF:
            {
                switch(event.type) {

                    case EVT_BUTTON_PRESSED:
                        {
                            DebugOutput("ST_ALARM_OFF", "EVT_BUTTON_PRESSED");        /** DEBUG! **/
                            current_state = ST_ALARM_OFF;                       
                        }
                    break; 

                    case EVT_CONTINUE:
                        {
                            DebugOutput("ST_ALARM_OFF", "EVT_CONTINUE");
                            current_state = ST_ALARM_LIGHTING_MUSIC;
                        }
                    break; 

                    default:
                        {
                            DebugOutput("ST_ALARM_LIGHTING", "EVT_UNKNOWN");                   /** DEBUG! **/
                        }
                    break;

                }
            }
            break;

    }
} 
  
///////////////////////////////////////////////////////////////////////////////
// Funciones de arduino 
void setup()
{
    initiate();
}

void loop()
{
    alarm_state_machine();
}