#include "Node.h"
#include "Sodaq_DS3231.h"

#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>
#include <SoftwareSerial.h>

#define NRF24_CHANNEL 88  // Канал радиомодуля.
RF24 radio(9, 10);               // nRF24L01 инициализировать модуль на пинах 9 и 10 Для Уно
RF24Network network(radio);      // Include the radio in the network
const uint16_t this_node = 00;

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
  network.update();
  // Получение данных
  while ( network.available() ) {
    RF24NetworkHeader header;
    Payload incomingPayload;
    network.read(header, &incomingPayload, sizeof(incomingPayload));

//    Serial.print(header.id, DEC);
//    Serial.print(";");
//    Serial.print(header.from_node, DEC);
//    Serial.print(";");
//    Serial.println(getDateTimeStr(incomingPayload));

    BTSerial.print(header.id, DEC);
    BTSerial.print(";");
    BTSerial.print(header.from_node, DEC);
    BTSerial.print(";");
    BTSerial.println(getDateTimeStr(incomingPayload));
  }  

}
