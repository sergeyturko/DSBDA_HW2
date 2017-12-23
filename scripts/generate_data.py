#!/usr/bin/env python3
"""Generate syslog log"""

import random
import datetime
import sys

# message types: debug(7), info(6), notice(5), warning/warn(4), error(3), crit(2), alert(1), emerg(0)
priority_level_list = ['debug', 'info', 'notice', 'warning', 'error', 'crit', 'alert', 'emerg']
facility_level_list = ['auth', 'authpriv', 'cron', 'daemon', 'ftp', 'kern', 'lpr', 'mail', 'mark', 'news', 'security', 'syslog', 'user', 'uucp']


if __name__ == '__main__':
    try:
        num_str = int(sys.argv[1])
    except:
        print 'Error: Set amount of line!'
        sys.exit()

    with open("syslog.log", "w") as file:
        for index in range(num_str) :
            # generate random message
            fl = random.choice(facility_level_list)
            pl = random.randint(0, 7)
            priority = priority_level_list[pl]
            month = random.randint(1, 12)
            day = random.randint(1, 27)
            hour = random.randint(0, 23)
            minute = random.randint(0, 59)
            second = random.randint(0, 59)
            d = datetime.date(year=1970, month=month, day=day)

            msg = "<%i> %s %02i %02i:%02i:%02i 127.0.0.1 %s: %s syslog message\n" % (pl, d.strftime("%b"), day, hour, minute, second, fl, priority)

            file.write(msg)
