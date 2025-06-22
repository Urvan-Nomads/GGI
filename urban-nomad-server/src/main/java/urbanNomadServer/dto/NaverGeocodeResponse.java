package urbanNomadServer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NaverGeocodeResponse {
    private String status;
    private List<Address> addresses;
    private String errorMessage;

    @Getter
    @Setter
    public static class Address {
        private String roadAddress;
        private String jibunAddress;
        private String englishAddress;
        private String x; // 경도
        private String y; // 위도

    }
}
