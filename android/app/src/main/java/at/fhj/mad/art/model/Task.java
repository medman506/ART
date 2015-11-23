package at.fhj.mad.art.model;

import java.util.Calendar;
import java.util.Locale;

/**
 * Model for a Basic Task the User should do
 */
public class Task {

    private String message;
    private String topic;
    private String link;
    private String address;
    private Calendar c;
    private long id;
    private int hour, minute, seconds, day, month, year;

    public Task() {
        setC(Calendar.getInstance());
        setSeconds(c.get(Calendar.SECOND));
        setMinute(c.get(Calendar.MINUTE));
        setHour(c.get(Calendar.HOUR_OF_DAY));
        setDay(c.get(Calendar.DAY_OF_MONTH));
        setMonth(c.get(Calendar.MONTH) + 1);
        setYear(c.get(Calendar.YEAR));
    }

    public Task(int id) {
        setC(Calendar.getInstance());
        setId(id);
        setSeconds(c.get(Calendar.SECOND));
        setMinute(c.get(Calendar.MINUTE));
        setHour(c.get(Calendar.HOUR_OF_DAY));
        setDay(c.get(Calendar.DAY_OF_MONTH));
        setMonth(c.get(Calendar.MONTH) + 1);
        setYear(c.get(Calendar.YEAR));
    }

    @Override
    public String toString() {
        return "Task{" +
                "message='" + message + '\'' +
                ", topic='" + topic + '\'' +
                ", hour=" + hour +
                ", minute=" + minute +
                ", seconds=" + seconds +
                ", day=" + day +
                ", month=" + month +
                ", year=" + year +
                '}';
    }

    public String getDayTime() {
        int hour = getHour();
        int minute = getMinute();
        int second = getSeconds();

        if (hour < 10 && minute < 10 && second < 10) {
            // 01:02:03
            return "0" + hour + ":" + "0" + minute + ":" + "0" + second;
        } else if (hour < 10 && minute < 10 && second > 9) {
            // 01:02:15
            return "0" + hour + ":" + "0" + minute + ":" + second;
        } else if (hour < 10 && minute > 9 && second < 10) {
            // 01:15:03
            return "0" + hour + ":" + minute + ":" + "0" + second;
        } else if (hour > 9 && minute < 10 && second < 10) {
            // 11:02:03
            return hour + ":" + "0" + minute + ":" + "0" + second;
        } else if (hour > 9 && minute > 9 && second < 10) {
            // 11:25:03
            return hour + ":" + minute + ":" + "0" + second;
        } else if (hour < 10 && minute > 9 && second > 9) {
            // 08:25:52
            return "0" + hour + ":" + minute + ":" + second;
        } else if (hour > 9 && minute < 10 && second > 9) {
            // 11:05:52
            return hour + ":" + "0" + minute + ":" + second;
        } else {
            // 15:25:26
            return hour + ":" + minute + ":" + second;
        }

    }

    public String getDate() {
        String zmonth, zday, date;

        String lang = Locale.getDefault().getLanguage();

        int year = getYear();
        int month = getMonth();
        int day = getDay();

        if (month < 10) {
            zmonth = "0" + month;
            if (day < 10) {
                zday = "0" + day;
                if (lang.equals("de")) {
                    date = zday + "." + zmonth + "." + year;
                } else {
                    date = zmonth + "." + zday + "." + year;
                }
            } else {
                if (lang.equals("de")) {
                    date = day + "." + zmonth + "." + year;
                } else {
                    date = zmonth + "." + day + "." + year;
                }
            }
        } else {
            if (day < 10) {
                zday = "0" + day;
                if (lang.equals("de")) {
                    date = zday + "." + month + "." + year;
                } else {
                    date = month + "." + zday + "." + year;
                }
            } else {
                if (lang.equals("de")) {
                    date = day + "." + month + "." + year;
                } else {
                    date = month + "." + day + "." + year;
                }
            }
        }

        return date;
    }

    // Getter & Setter
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Calendar getC() {
        return c;
    }

    private void setC(Calendar c) {
        this.c = c;
    }

    public long getId() {
        return id;
    }

    private void setId(long id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

}
