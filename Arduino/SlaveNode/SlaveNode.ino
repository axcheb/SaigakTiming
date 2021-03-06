#include "Node.h"
#include <EEPROM.h>
#include "Sodaq_DS3231.h"

#include <RF24Network.h>
#include <RF24.h>
#include <SPI.h>

// Адрес этого датчика. 
// Если датчиков несколько, то номера нод у них должны отличаться и быть в промежутке 01 - 05.
const uint16_t this_node = 01;
const uint16_t master_node = 00;

//#define PRINT_TO_SERIAL  // Печать в serial для отладки. Желательно отключать.

#define EE_RESET 1         // Любое число 0-255, чтоб сбросить настройки и время.
#define NRF24_CHANNEL 88  // Канал радиомодуля. 

// Пин фоторезистора. Использван A0 как цифровой.
#define PIN_PHOTO_SENSOR A0

// Таймаут антидребезга, миллисекунды.
#define DEBOUNCE 1000

unsigned long debounceTimer;

volatile unsigned long millisOnSecondStart;

RF24 radio(9, 10);               // nRF24L01 инициализировать модуль на пинах 9 и 10 Для Уно
RF24Network network(radio);      // Include the radio in the network

volatile Payload* payload = NULL;

void setup() {
  Serial.begin(9600);
  SPI.begin();
  radio.begin();
  rtc.begin();
  adjustTime();
  network.begin(NRF24_CHANNEL, this_node);  //(channel, node address)
  //  radio.setPALevel(RF24_PA_MAX); // выкрутить мощьность на максимум
  radio.setDataRate(RF24_250KBPS); // чем меньше скорость, тем выше дальность приёма
  initInterrupts();
}

void initInterrupts() {

  // D2 (прерывание 0) Для подсчёта миллисекунд.
  attachInterrupt(0, rememberSecondMillis, FALLING);

  // Enable RTC Interrupt
  rtc.enableInterrupts(EverySecond);
}

void loop() {
  fotorezistor();
  handleNetwork();
}

void handleNetwork() {
  network.update();

  // Получение данных
  while ( network.available() ) {
    RF24NetworkHeader header;
    Payload incomingPayload;
    network.read(header, &incomingPayload, sizeof(incomingPayload));
    handleIncomingPayload(incomingPayload);
  }

  // Отправка данных
  Payload* p = payload;
  if (payload != NULL) {
    RF24NetworkHeader header(/*to node*/ master_node);
    bool ok = network.write(header, p, sizeof(*p));

    if (ok) {
      #ifdef PRINT_TO_SERIAL
      Serial.print("ok sending:");
      Serial.print(p->command);
      Serial.print(",");
      Serial.println(getDateTimeStr(*p));
      #endif
      payload = NULL;
    } else {
      #ifdef PRINT_TO_SERIAL
      Serial.println("failed.");
      #endif
    }
  }
}


void handleIncomingPayload(Payload incomingPayload) {
    #ifdef PRINT_TO_SERIAL
    Serial.print("Incoming time:");
    Serial.println(getDateTimeStr(incomingPayload));
    #endif

    switch(incomingPayload.command) {
        case 't': //time
            adjustTime(incomingPayload);
        case 's': //search
            int ms = getTimerMillis();
            unsigned long timeSeconds = rtc.now().getEpoch();
            Payload p = {'t', timeSeconds, ms};
            // Костыль. Без задержки между приёмом и отправкой сообщения возникают фиерические глюки.
            delay(10);
            payload = &p;
            break;
    }
}

void adjustTime(Payload incomingPayload) {
    rtc.setDateTime(rtc.makeDateTime(incomingPayload.timeSeconds));
}


void adjustTime() {
  if (EEPROM.read(0) != EE_RESET) {   // первый запуск
    EEPROM.write(0, EE_RESET);
    rtc.setDateTime(DateTime(__DATE__, __TIME__));
  }
}

/**
    С миллисекундами вышла засада. DS3231 считает время с точностю до секунды.
    Считаю миллисекунды руками на микроконтроллерере с не самой высокой точностью.
*/
void rememberSecondMillis() {
  millisOnSecondStart = millis();
}


int getTimerMillis() {
  return (millis() - millisOnSecondStart) % 1000;
}


void fotorezistor() {
  // Инверсия. Датчик срабатывает, когда прерывается световой поток.
  boolean fotorezistorTriggered = !digitalRead(PIN_PHOTO_SENSOR);

  if (fotorezistorTriggered) {
    int ms = getTimerMillis();
    DateTime now = rtc.now();    
    if ((millis() - debounceTimer) > DEBOUNCE) {
      unsigned long timeSeconds = now.getEpoch();
      // запоминаю, а отправка произойдёт потом.
      Payload p = {'f', timeSeconds, ms};
      payload = &p;
      delay(5); // Костыль. Без этой задержки payload на master уходит кривой.
      #ifdef PRINT_TO_SERIAL
      Serial.println(getDateTimeStr(p));
      #endif
    }
    // Таймер антидребезга
    debounceTimer = millis();
  }

}
