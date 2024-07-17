package com.myhexin.cragent.coderabbittest;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlarmChecker {
    
    private static final ConcurrentHashMap<AlarmEvent, Long> EVENT_MAP = new ConcurrentHashMap<>();

    public boolean needAlarm(AlarmModule module, String identity) {
        AlarmEvent alarmEvent = new AlarmEvent(module, identity);
        if (module.getExpiredSeconds() == 0) {
            EVENT_MAP.put(alarmEvent, alarmEvent.createdNanoTime);
            return true;
        } else {
            Long eventNanoTime = EVENT_MAP.compute(alarmEvent, (event, oldCreatedNanoTime) -> {
                if (isExistedEventExpire(event, oldCreatedNanoTime)) {
                    return event.createdNanoTime;
                } else {
                    return oldCreatedNanoTime;
                }
            });
            return alarmEvent.createdNanoTime.equals(eventNanoTime);
        }
    }
}
