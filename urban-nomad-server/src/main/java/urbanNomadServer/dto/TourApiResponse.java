package urbanNomadServer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TourApiResponse {
    private Response response;

    @Getter @Setter
    public static class Response {
        private Body body;
    }
    @Getter @Setter
    public static class Body {
        private Items items;
    }
    @Getter @Setter
    public static class Items {
        private List<Item> item;
    }
    @Getter @Setter
    public static class Item {
        private String contentid;
        private String title;
        private String addr1;
        private String addr2;
        private String mapy;
        private String mapx;
        private String overview;
        private String firstimage;
    }
}
