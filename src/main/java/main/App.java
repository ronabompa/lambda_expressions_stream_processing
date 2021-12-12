package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.lang.String;

/**
 * Created by Rona Dumitrescu on 06.06.2019.
 */
public class App {

   private List<MonitoredData> listMonitoredData = new LinkedList<MonitoredData>();

    // GETTERS & SETTERS
    public List<MonitoredData> getListMonitoredData() {
        return listMonitoredData;
    }

    public void setListMonitoredData(List<MonitoredData> listMonitoredData) {
        this.listMonitoredData = listMonitoredData;
    }

    //METHODS
    /**
     * This method reads MonitoredData from file
     * @return list of objects of type MonitoredData
     */
    public List<MonitoredData> readMonitoredData()
    {
        try
        {
            return Files.lines(Paths.get("Activities.txt")).map(line ->
            {
                MonitoredData monitoredData = new MonitoredData();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

                String[] info = line.split("\t\t"); // ficare linie o despartim in 3 prin cele 2 taburi

                try
                {
                    Date date = dateFormat.parse(info[0]); // prelucram start time
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                    monitoredData.setStartTime(localDateTime);

                    date = dateFormat.parse(info[1]);  // prelucram end time
                    localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
                    monitoredData.setEndTime(localDateTime);

                }catch (ParseException e)
                {
                    e.printStackTrace();
                }

                monitoredData.setActivity(info[2]);

                return monitoredData;
                }).collect(Collectors.toList());

        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This function counts how many days of monitored data appears in the log
     * @return the number of days counted
     */
    public Integer howManyDays()
    {
        Integer days = this.getListMonitoredData()
                          .stream()
                          .collect(HashSet::new, (set, element) ->
                          {
                              set.add(element.getStartTime().getDayOfYear() + "-" + element.getStartTime().getMonth() + "-" + element.getStartTime().getDayOfMonth());
                          }, HashSet::addAll)
                          .size();
        return days;
    }

    /**
     * Aceasta metoda afla de cate ori a aparut fiecare activitate pe intreaga perioada de monitorizare
     * @return a map of Activites as String and How many times an Activity was
     */
    public Map<String,Long> activityFrequencyEntireMonitoringPeriod()
    {
        Map<String, Long> activityFrequency = this.getListMonitoredData()
                                                 .stream()
                                                 .collect(Collectors.groupingBy(MonitoredData::getActivity, Collectors.counting()));

        return activityFrequency;
    }

    /**
     * Aceasta metoda afla de cate ori a aparut fiecare activitate pentru fiecare zi din perioada de monitorizare
     * @return un Linked SashMap de Activitati si o mapa de zile si de cate ori s-a efectuat activitatea in acea zi
     */
    public  LinkedHashMap<String, Map<LocalDateTime, Long>>  activityFrequencyEachDayMonitoringPeriod()
    {
//        Map<String, Map<LocalDateTime, Long>> activityFrequencyEachDay = this.getListMonitoredData()
//                                                                            .stream()
//                                                                           // .collect(Collectors.groupingBy(MonitoredData :: getActivity, this.getListMonitoredData().stream().collect(Collectors.groupingBy(MonitoredData::getStartTime.toString(), Collectors.counting()))));
//
        LinkedHashMap<String, Map<LocalDateTime, Long>> activityFrequencyEachDay =
                getListMonitoredData().stream()
                        .collect(Collectors.groupingBy(MonitoredData::getActivity, Collectors.groupingBy(MonitoredData::getStartTime, Collectors.counting())))
                        .entrySet().stream().collect(LinkedHashMap::new, (k,v) ->{
                    k.put(v.getKey(), v.getValue());
                }, LinkedHashMap::putAll);

        return activityFrequencyEachDay;
    }

    /**
     * Aceasta metoda afla durata fiecarei activitati trecute in fisier
     * @return O mapa cu activitatea si durata ei
     */
    public  Map<String, Duration> activityDurationEachLine()
    {
        Map<String, Duration>  activityDurationEachLineMap = this.getListMonitoredData()
                .stream()
                .collect( LinkedHashMap::new, (k,v) -> k.put(v.getActivity() + v.getStartTime(),Duration.between(v.getStartTime(),v.getEndTime()).abs()), LinkedHashMap::putAll); // pe key punem activity si pe value durata

        return activityDurationEachLineMap;
    }

    /**
     * Aceasta metoda afla durata fiecarei activitati pe intreaga perioada de monintorizare
     * @return O mapa cu ativitatea si durata ei pe intreaga perioada de monitorizare
     */
    public  Map<String, Duration> activityEntireDuration()
    {
        Map<String, Duration>  activityDurationEachLineMap = this.getListMonitoredData()
                .stream()
                .collect(Collectors.groupingBy(MonitoredData::getActivity, Collectors.summingLong(m -> Duration.between(m.getStartTime(),m.getEndTime()).abs().getSeconds()))) // pe key punem activity si pe value durata
                .entrySet().stream().collect(LinkedHashMap::new, (k,v) -> k.put(v.getKey(), Duration.of(v.getValue(), ChronoUnit.SECONDS)),LinkedHashMap::putAll);
        return activityDurationEachLineMap;
    }

    /**
     * Aceasta activitate care s-au desfasurat in 5 mai putin de 5 min in 90% dintre cazuri
     * @return Lista de activitati eligibile
     */
    public List<String> majorityOfActivitiesLessThan5min()
    {
         List<String> activitiesLess5min90percent = this.getListMonitoredData()
                .stream()
                .collect(Collectors.groupingBy(MonitoredData::getActivity, Collectors.mapping(m ->
                        Duration.between(m.getStartTime(),m.getEndTime()).abs(), Collectors.toList()))) // pe key punem activity si pe value durata
                .entrySet()
                .stream()
                .filter(m->
                {
                    int countLess5min = m.getValue()
                            .stream()
                            .filter(activity -> activity.getSeconds() <= 300)
                            .collect(Collectors.toList()).size();
                    return  (double)(m.getValue().size()) * 0.9 <= (double)countLess5min;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return  activitiesLess5min90percent;
    }

    public static void main(String[] args)
    {
        App app = new App();

        app.setListMonitoredData(app.readMonitoredData());

        System.out.println("");
        System.out.println("Numarul de zile din perioada de monitorizare:");
        System.out.println(app.howManyDays());

        System.out.println("");
        System.out.println("Activitatile si de cate ori s-au desfasurat pe intreaga perioada de monitorizre:");
        System.out.println(app.activityFrequencyEntireMonitoringPeriod());

        System.out.println("");
        System.out.println("Activitatile si de cate ori s-au desfasurat in fiecare zi:");
        System.out.println(app.activityFrequencyEachDayMonitoringPeriod());

        System.out.println("");
        System.out.println("Durata fiecarei acitivitati introduse pentru fiecare linie:");
        System.out.println(app.activityDurationEachLine());

        System.out.println("");
        System.out.println("Durata fiecare acitivitati pe intreaga perioada de monitorizare:");
        System.out.println(app.activityEntireDuration());

        System.out.println("");
        System.out.println("Activitatile care au durat mai putin de 5 min 90% din timp:");
        System.out.println(app.majorityOfActivitiesLessThan5min());

    }
}
