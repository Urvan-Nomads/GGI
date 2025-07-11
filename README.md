# Urban Nomad Server

## 프로젝트 개요

Urban Nomad Server는 한국관광공사와 원티드랩이 주관한 프롬프톤 참여 프로젝트입니다. 원티드랩의 LaaS(LLM as a Service)를 활용하여 개발되었으며, 사용자와의 자연어 채팅을 통해 맞춤형 여행 경로를 추천하고 시각화된 결과물을 제공하는 것을 목표로 합니다.

사용자가 채팅을 통해 원하는 여행지와 관심사를 입력하면, LaaS에 연결된 한국관광공사 Tour API를 통해 관련 정보를 조회합니다. 조회된 여행지 정보(사진, 주소, 설명 등)를 사용자에게 보여주고, 사용자가 확정하면 최적의 경로를 계산하여 Thymeleaf로 서버 사이드 렌더링된 웹 페이지 링크를 제공합니다. 사용자는 이 링크를 통해 지도 위에 표시된 경로와 여행지 정보를 카드 형태로 한눈에 확인할 수 있습니다.

## 데모 영상

[![Urban Nomad Demo Video](https://img.youtube.com/vi/6_-wBfjQKFU/0.jpg)](https://youtu.be/6_-wBfjQKFU?si=7j50YibX1GNmvwDa)

(썸네일을 클릭하면 데모 영상을 시청할 수 있습니다.)

## 주요 기능

-   **자연어 기반 여행지 추천:** 사용자와의 채팅을 통해 원하는 여행 스타일, 관심사, 지역 등을 파악하고 맞춤형 여행지를 추천합니다. (LaaS)
-   **실시간 여행 정보 제공:** 한국관광공사 Tour API와 연동하여 여행지의 상세 정보(사진, 주소, 설명 등)를 실시간으로 제공합니다.
-   **간편한 경로 설정:** 채팅으로 간편하게 출발지와 경유지를 설정하고 최종 경로를 확정할 수 있습니다.
-   **최적 경로 계산:** 설정된 출발지와 경유지를 바탕으로, Greedy 알고리즘과 Haversine 거리 계산 공식을 사용하여 최적의 여행 경로를 계산합니다.
-   **시각화된 결과 제공:** 계산된 경로는 Naver Maps API를 통해 지도 위에 시각적으로 표시되며, 각 여행지 정보는 카드 형태로 제공되어 사용자 편의성을 높였습니다.

## 기술 스택

### Backend

-   Java 17
-   Spring Boot 3.5.0
-   Spring Web
-   Thymeleaf (SSR)

### Frontend

-   HTML, CSS, JavaScript
-   Naver Maps API

### LaaS (LLM as a Service)

-   **Wanted LaaS:** 원티드랩에서 제공하는 LLM 기반 서비스 개발 및 운영 솔루션입니다. 본 프로젝트에서는 LaaS의 주요 기능들을 활용하여 개발 생산성을 높이고, 자연어 처리 및 API 연동을 효율적으로 구현했습니다.

-   **API Connector 상세 설명:**
    -   LaaS의 핵심 기능 중 하나인 API Connector는 LLM이 사용자의 질문에 답변하기 위해 필요한 외부 API를 스스로 찾아서 호출하도록 만드는 강력한 도구입니다.
    -   **자연어 기반 API 탐색 및 호출:** 개발자는 API Connector를 생성할 때, 해당 API가 어떤 역할을 하는지 자연어로 상세하게 설명(Description)을 작성합니다. 사용자가 질문을 하면, LLM은 질문의 의도를 파악하고 가장 적합한 역할을 가진 API Connector를 스스로 찾아냅니다.
    -   **동적 파라미터 매핑:** API의 엔드포인트 주소(e.g., `/waypoint?contentId={contentId}`)에 `{변수}` 형태로 파라미터를 설정할 수 있습니다. LLM은 사용자의 질문 내용에서 이 변수에 해당하는 특정 단어나 문장(e.g., "경포대")을 추출하여 동적으로 매핑하고 API를 호출합니다. 이를 통해 정적인 응답이 아닌, 사용자의 요구에 맞는 동적인 정보 조회가 가능해집니다.
    -   **Function Calling:** 위 과정을 통해 최종적으로 결정된 API는 LaaS의 Function Calling 기능을 통해 실제 백엔드 서버의 해당 엔드포인트로 호출됩니다.

-   **Sandbox:** LaaS가 제공하는 격리된 실행 환경을 통해 안전하게 코드를 실행하고 사용자에게 결과(웹 페이지 링크 등)를 전달합니다.

## 실행 방법

1.  **프로젝트 클론**

    ```bash
    git clone https://github.com/your-username/urban-nomad-server.git
    ```

2.  **프로젝트 디렉토리로 이동**

    ```bash
    cd urban-nomad-server
    ```

3.  **API 키 설정**

    -   `src/main/resources/application.yml` 파일을 열고 아래 항목에 자신의 API 키를 입력합니다.
        -   `tourapi.service-key`: 한국관광공사 Tour API 서비스 키
        -   `naver.map.client_id`: Naver Maps API 클라이언트 ID
        -   `naver.map.client_secret`: Naver Maps API 클라이언트 시크릿

4.  **애플리케이션 실행**

    ```bash
    ./gradlew bootRun
    ```

5.  **LaaS 연동 및 테스트**

    -   원티드랩 LaaS의 Playground 또는 채팅 인터페이스를 통해 아래와 같이 자연어 명령을 실행하여 테스트합니다.
    -   예시:
        -   "서울에서 출발해서 강릉으로 여행갈 건데, 경포대랑 주문진 수산시장을 꼭 들르고 싶어."
        -   LaaS는 이 명령을 해석하여 `/departure`, `/waypoint` 엔드포인트를 순차적으로 호출하고, 최종적으로 `/done` 엔드포인트를 호출하여 결과 페이지 링크를 반환합니다.

## API 엔드포인트 (LaaS 호출용)

-   `GET /departure?address={address}`: 출발지를 설정합니다.
-   `GET /waypoint?contentId={contentId}`: 경유지를 추가합니다.
-   `GET /done`: 최적 경로 계산을 완료하고 결과 페이지를 생성합니다.

