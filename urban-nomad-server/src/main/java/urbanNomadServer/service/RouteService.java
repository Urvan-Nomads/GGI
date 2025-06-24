package urbanNomadServer.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;
import urbanNomadServer.dto.NaverGeocodeResponse;
import urbanNomadServer.dto.TourApiResponse;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

@Service
public class RouteService {

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Value("${naver.map.client-id}")
    private String clientId;

    @Value("${naver.map.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    private List<Waypoint> waypoints = new ArrayList<>();

    @Getter
    private List<Waypoint> fullWaypoints = new ArrayList<>();

    @Getter
    private Waypoint departure;

    // 출발지 저장 (list의 0번 index 에 저장)
    public boolean setDeparture(String address) {
        // 출발지가 이미 설정되어 있으면 변경하지 않음
        if (!waypoints.isEmpty() && waypoints.get(0).getId() == 0) {
            return false;
        }
        NaverGeocodeResponse.Address addr = getFirstAddress(address);
        if (addr == null) {
            System.err.println("좌표 변환 실패: " + address);
            return false;
        }
        Double lat = Double.parseDouble(addr.getY());
        Double lng = Double.parseDouble(addr.getX());

        departure = new Waypoint(
                0,
                "출발지",
                address,
                lat,
                lng,
                "출발지",
                null
        );

        waypoints.add(0, departure);
        return true;
    }

    private NaverGeocodeResponse.Address getFirstAddress(String address) {
        try {
//            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=" + address;

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-ncp-apigw-api-key-id", clientId);
            headers.set("x-ncp-apigw-api-key", clientSecret);
            headers.set("Accept", "application/json");

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<NaverGeocodeResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    NaverGeocodeResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                NaverGeocodeResponse body = response.getBody();
                if ("OK".equals(body.getStatus()) && body.getAddresses() != null && !body.getAddresses().isEmpty()) {
                    return body.getAddresses().get(0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addWaypointByContentId(int contentId) {
        // 출발지(contentId=0)는 중복 체크에서 제외, 나머지 중복 체크
        for (int i = 1; i < waypoints.size(); i++) {
            if (waypoints.get(i).getId() == contentId) {
                return false;
            }
        }
        if (waypoints.size() >= 11) { // 출발지+최대 10개
            return false;
        }
        Waypoint waypoint = fetchWaypointFromApi(contentId);
        if (waypoint == null) return false;
        waypoints.add(waypoint);

        // 추가: 현재 waypoints 리스트 출력
        System.out.println("=== Waypoints 추가 후 현재 순서 ===");
        printWaypoints(waypoints);

        return true;
    }

    public Waypoint fetchWaypointFromApi(int contentId) {
        try {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl("https://apis.data.go.kr/B551011/KorService2/detailCommon2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileApp", "AppTest")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("pageNo", 1)
                    .queryParam("numOfRows", 10)
                    .queryParam("contentId", contentId)
                    .queryParam("_type", "json")
                    .build(true)
                    .toUri();

            ResponseEntity<TourApiResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    TourApiResponse.class
            );

            TourApiResponse.Body body = response.getBody().getResponse().getBody();
            if (body == null || body.getItems() == null || body.getItems().getItem() == null || body.getItems().getItem().isEmpty())
                return null;

            TourApiResponse.Item item = body.getItems().getItem().get(0);

            String name = item.getTitle();
            String address = (item.getAddr1() != null ? item.getAddr1() : "") +
                    (item.getAddr2() != null ? " " + item.getAddr2() : "");
            Double lat = item.getMapy() != null ? Double.parseDouble(item.getMapy()) : null;
            Double lng = item.getMapx() != null ? Double.parseDouble(item.getMapx()) : null;
            String description = item.getOverview();
            String imageUrl = item.getFirstimage();

            return new Waypoint(
                    Integer.parseInt(item.getContentid()),
                    name,
                    address,
                    lat,
                    lng,
                    description,
                    imageUrl
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 경로 재계산 (greedy)
    public void greedy() {
        Waypoint departure = waypoints.get(0);

        System.out.println("=== done() 호출 전 waypoints 순서 ===");
        printWaypoints(waypoints);

        // 출발지 제외 나머지 waypoint 리스트
        List<Waypoint> targets = new ArrayList<>(waypoints.subList(1, waypoints.size()));
        List<Waypoint> ordered = new ArrayList<>();
        boolean[] visited = new boolean[targets.size()];
        int visitedCount = 0;

        double currLat = departure.getLatitude();
        double currLng = departure.getLongitude();

        // 그리디: 출발지에서 가장 가까운 곳부터 방문
        while (visitedCount < targets.size()) {
            double minDist = Double.MAX_VALUE;
            int minIdx = -1;
            for (int i = 0; i < targets.size(); i++) {
                if (visited[i]) continue;
                Waypoint wp = targets.get(i);
                if (wp.latitude == null || wp.longitude == null) continue;
                double dist = haversine(currLat, currLng, wp.latitude, wp.longitude);
                if (dist < minDist) {
                    minDist = dist;
                    minIdx = i;
                }
            }
            if (minIdx == -1) break;
            ordered.add(targets.get(minIdx));
            visited[minIdx] = true;
            currLat = targets.get(minIdx).latitude;
            currLng = targets.get(minIdx).longitude;
            visitedCount++;
        }

        // 출발지 + 최적화된 나머지 순서로 재조립
        List<Waypoint> finalOrder = new ArrayList<>();
        finalOrder.add(departure);
        finalOrder.addAll(ordered);
        waypoints = finalOrder;

        System.out.println("=== done() 후 최적 경로 순서 ===");
        printWaypoints(finalOrder);

//        clear();
    }

    public void excludeDeparture(){
        waypoints.remove(0);

        System.out.println("=== excludeDeparture 이후 ===");
        printWaypoints(waypoints);
    }

    private double haversine(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public void clear() {
        waypoints.clear();
    }


    public void makeFullWaypoints() {
        fullWaypoints.add(departure);
        fullWaypoints.addAll(waypoints);
    }


    private void printWaypoints(List<Waypoint> list) {
        for (int i = 0; i < list.size(); i++) {
            Waypoint wp = list.get(i);
            System.out.printf("[%d] contentId=%d, name=%s, address=%s (lat=%.6f, lng=%.6f)%n",
                    i, wp.getId(), wp.getName(), wp.getAddress(),
                    wp.getLatitude() != null ? wp.getLatitude() : 0.0,
                    wp.getLongitude() != null ? wp.getLongitude() : 0.0);
        }
    }

    @Getter
    @Setter
    public static class Waypoint {
        private int id;
        private String name;
        private String address;
        private Double latitude;
        private Double longitude;
        private String description;
        private String imageUrl;

        public Waypoint(int id, String name, String address, Double latitude, Double longitude, String description, String imageUrl) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
            this.description = description;
            this.imageUrl = imageUrl;
        }
    }


}
