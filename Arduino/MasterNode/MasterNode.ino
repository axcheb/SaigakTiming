#include "Node.h"
#include "Sodaq_DS3231.h"

#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>
#include <SoftwareSerial.h>
#include <stdlib.h>

//#define PRINT_TO_SERIAL  // Печать в serial для отладки. Желательно отключать.
#define NRF24_CHANNEL 88  // Канал радиомодуля.
RF24 radio(9, 10);               // nRF24L01 инициализировать модуль на пинах 9 и 10 Для Уно
RF24Network network(radio);      // Include the radio in the network
const uint16_t this_node = 00;

const uint16_t node1 = 01;
const uint16_t node2 = 02;

const int btRxPin = 3;
const int btTxPin = 2;

SoftwareSerial BTSerial(btRxPin, btTxPin);

void setup() {
  Serial.begin(9600);
  BTSerial.begin(9600);
  SPI.begin();
  radio.begin();
  network.begin(NRF24_CHANNEL, this_node);  //(channel, node address)
//  radio.setPALevel(RF24_PA_MAX); // выкрутить мощьность на максимум
  radio.setDataRate(RF24_250KBPS); // чем меньше скорость, тем выше дальность приёма
}

void loop() {
  static byte prevAv = 0;
  static uint32_t tmr = 0;
  byte av = BTSerial.available();
  if (av != prevAv) {
    prevAv = av;
    tmr = millis();
  }
  if ((av && millis() - tmr > 10) || av > 60) {
    char str[30];
    int amount = BTSerial.readBytesUntil('\n', str, 30);
    str[amount] = NULL;

    #ifdef PRINT_TO_SERIAL
    Serial.println(str);
    #endif

    char command = str[0];
    if (command == 's') { //search
        char command = 's';
        unsigned long timeSeconds = 0;
        int ms = 0;
        Payload p = {command, timeSeconds, ms};
        RF24NetworkHeader header1(/*to node*/ node1);
        bool ok = network.write(header1, &p, sizeof(p));
        RF24NetworkHeader header2(/*to node*/ node2);
        #ifdef PRINT_TO_SERIAL
        Serial.println(ok);
        #endif
        ok = network.write(header2, &p, sizeof(p));
        #ifdef PRINT_TO_SERIAL
        Serial.println(ok);
        #endif
    } else if (command == 't') {//time
        char* nextSubStr = strchr(str, ',');
        if (nextSubStr) {
            nextSubStr ++;
            unsigned long timeSeconds = strtoull(nextSubStr);
            char command = 't';
            int ms = 0;
            Payload p = {command, timeSeconds, ms};
            RF24NetworkHeader header1(/*to node*/ node1);
            bool ok = network.write(header1, &p, sizeof(p));
            #ifdef PRINT_TO_SERIAL
            Serial.println(ok);
            #endif
            RF24NetworkHeader header2(/*to node*/ node2);
            ok = network.write(header2, &p, sizeof(p));
            #ifdef PRINT_TO_SERIAL
            Serial.println(ok);
            #endif
        }
    }
  }

   // Получение данных
  network.update();
  while ( network.available() ) {
    RF24NetworkHeader header;
    Payload incomingPayload;
    network.read(header, &incomingPayload, sizeof(incomingPayload));

    #ifdef PRINT_TO_SERIAL
    Serial.print(incomingPayload.command);
    Serial.print(header.from_node, DEC);
    Serial.print(",");
    Serial.print(incomingPayload.timeSeconds, DEC);
    Serial.print(",");
    Serial.println(incomingPayload.ms, DEC);
    #endif

//    Serial.print(header.id, DEC);
    BTSerial.print(incomingPayload.command);
    BTSerial.print(",");
    BTSerial.print(header.from_node, DEC);
    BTSerial.print(",");
    BTSerial.print(incomingPayload.timeSeconds, DEC);
    BTSerial.print(",");
    BTSerial.println(incomingPayload.ms, DEC);
  }  

}

unsigned long strtoull(char* str) {
    unsigned long long res = 0;
    int length = strlen(str);
    for (int i = 0; i < length; i++) {
        char c = str[i];
        if (c < '0' || c > '9') break;
        res *= 10;
        res += (c - '0');
    }
    return res;
}
