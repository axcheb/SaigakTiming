#ifndef SAIGAKTIMING_NODE_H
#define SAIGAKTIMING_NODE_H

#include "Sodaq_DS3231.h"

// Структура пересылаемых данных:
struct Payload {
  // количество секунд 2000/01/01
  unsigned long timeSeconds;
  // количество миллисекунд
  int ms;
};

/**
   Возвращает время строкой. Для отладки.
*/
String getDateTimeStr(Payload p) {
  DateTime now = DateTime(p.timeSeconds);
  char buffer[30];
  sprintf(buffer, "%u-%02d-%02dT%02d:%02d:%02d.%03d",
          now.year(),
          now.month(),
          now.date(),
          now.hour(),
          now.minute(),
          now.second(),
          p.ms
         );
  return String(buffer);
}

#endif //SAIGAKTIMING_NODE_H
