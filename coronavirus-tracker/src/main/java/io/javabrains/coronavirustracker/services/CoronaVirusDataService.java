package io.javabrains.coronavirustracker.services;

import io.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private static String VIRUS_DEATH_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

    private List<LocationStats> allCases = new ArrayList<>();
    private List<LocationStats> allDeaths = new ArrayList<>();
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllCases() {
        return allCases;
    }

    public List<LocationStats> getAllDeaths() {
        return allDeaths;
    }

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
    @Scheduled(cron = "* * * 1 * *")
    public void  fetchVirusData() throws IOException, InterruptedException {
        fetchVirusCases();
        fetchVirusDeaths();
        assembleDatas();

    }

    public void fetchVirusCases() throws IOException, InterruptedException {
        List<LocationStats> newCases = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvBodyReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStat = new LocationStats();

            String state = record.get("Province/State");
            locationStat.setState(state);
            String country = record.get("Country/Region");
            locationStat.setCountry(country);
            int latestTotalCases = Integer.parseInt(record.get(record.size() - 1));
            locationStat.setLatestTotalCases(latestTotalCases);
            int prevTotalCase = Integer.parseInt(record.get(record.size() - 2));
            locationStat.setDiffFromPrevDay(latestTotalCases - prevTotalCase);

            newCases.add(locationStat);
        }

        this.allCases = newCases;
    }

    public void fetchVirusDeaths() throws IOException, InterruptedException {
        List<LocationStats> newDeaths = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DEATH_URL))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvBodyReader = new StringReader(httpResponse.body());

        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationDeath = new LocationStats();

            String state = record.get("Province/State");
            locationDeath.setState(state);
            String country = record.get("Country/Region");
            locationDeath.setCountry(country);
            int latestTotalDeaths = Integer.parseInt(record.get(record.size() - 1));
            locationDeath.setLatestTotalDeaths(latestTotalDeaths);
            int prevTotalDeath = Integer.parseInt(record.get(record.size() - 2));
            locationDeath.setDeathDiffFromPrevDay(latestTotalDeaths - prevTotalDeath);

            newDeaths.add(locationDeath);
        }

        this.allDeaths = newDeaths;
    }

    public void assembleDatas(){
        List<LocationStats> newDatas = new ArrayList<>();
        if (this.allDeaths.size() == this.allCases.size()){
            for(int i = 0; i < this.allCases.size(); i++){
                LocationStats locationStats = new LocationStats();
                String state = this.allCases.get(i).getState();
                locationStats.setState(state);
                String country = this.allCases.get(i).getCountry();
                locationStats.setCountry(country);
                int latestTotalCases = this.allCases.get(i).getLatestTotalCases();
                locationStats.setLatestTotalCases(latestTotalCases);
                int diffFromPrevDay = this.allCases.get(i).getDiffFromPrevDay();
                locationStats.setDiffFromPrevDay(diffFromPrevDay);
                int latestTotalDeaths = this.allDeaths.get(i).getLatestTotalDeaths();
                locationStats.setLatestTotalDeaths(latestTotalDeaths);
                int deathDiffFromPrevDay = this.allDeaths.get(i).getDeathDiffFromPrevDay();
                locationStats.setDeathDiffFromPrevDay(deathDiffFromPrevDay);

                newDatas.add(locationStats);
            }

            this.allStats = newDatas;
        }


    }





}
