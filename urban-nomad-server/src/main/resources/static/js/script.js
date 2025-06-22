class LocationDashboard {
    constructor() {
        this.map = null
        this.markers = []
        this.currentInfoWindow = null
        this.init()
    }

    init() {
        this.initMap()
        this.bindEvents()
        this.loadLocations()
        this.initSearch()
    }

    initMap() {
        const mapOptions = {
            center: new naver.maps.LatLng(window.mapCenter.lat, window.mapCenter.lng),
            zoom: 10,
            mapTypeControl: true,
            mapTypeControlOptions: {
                style: naver.maps.MapTypeControlStyle.BUTTON,
                position: naver.maps.Position.TOP_RIGHT,
            },
            zoomControl: true,
            zoomControlOptions: {
                style: naver.maps.ZoomControlStyle.SMALL,
                position: naver.maps.Position.TOP_LEFT,
            },
        }

        this.map = new naver.maps.Map("map", mapOptions)
        this.updateLocationDisplay()
    }

    loadLocations() {
        if (!window.locationData || window.locationData.length === 0) {
            return
        }

        window.locationData.forEach((location) => {
            this.addMarker(location)
        })

        // 첫 번째 위치로 지도 중심 이동
        if (window.locationData.length > 0) {
            const firstLocation = window.locationData[0]
            this.map.setCenter(new naver.maps.LatLng(firstLocation.latitude, firstLocation.longitude))
        }
    }

    addMarker(location) {
        const position = new naver.maps.LatLng(location.latitude, location.longitude)

        const marker = new naver.maps.Marker({
            position: position,
            map: this.map,
            title: location.name,
            icon: {
                content: this.createMarkerIcon(location),
                size: new naver.maps.Size(40, 40),
                anchor: new naver.maps.Point(20, 40),
            },
        })

        const infoWindow = new naver.maps.InfoWindow({
            content: this.createInfoWindowContent(location),
            maxWidth: 300,
            backgroundColor: "#ffffff",
            borderColor: "#e2e8f0",
            borderWidth: 1,
            anchorSize: new naver.maps.Size(10, 10),
            anchorSkew: true,
            anchorColor: "#ffffff",
            pixelOffset: new naver.maps.Point(0, -10),
        })

        naver.maps.Event.addListener(marker, "click", () => {
            if (this.currentInfoWindow) {
                this.currentInfoWindow.close()
            }
            infoWindow.open(this.map, marker)
            this.currentInfoWindow = infoWindow
            this.highlightLocationCard(location.id)
        })

        this.markers.push({ marker, infoWindow, location })
    }

    createMarkerIcon(location) {
        const color = location.active ? "#4299e1" : "#e53e3e"
        return `
            <div style="
                width: 32px;
                height: 32px;
                background: ${color};
                border: 3px solid white;
                border-radius: 50%;
                box-shadow: 0 2px 8px rgba(0,0,0,0.3);
                display: flex;
                align-items: center;
                justify-content: center;
                color: white;
                font-weight: bold;
                font-size: 12px;
            ">
                ${location.name.charAt(0).toUpperCase()}
            </div>
        `
    }

    createInfoWindowContent(location) {
        return `
            <div style="padding: 16px; min-width: 200px;">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <h3 style="margin: 0; color: #2d3748; font-size: 1.1rem; font-weight: 600;">${location.name}</h3>
                    <span style="
                        padding: 4px 8px;
                        border-radius: 12px;
                        font-size: 0.75rem;
                        font-weight: 600;
                        background: ${location.active ? "#c6f6d5" : "#fed7d7"};
                        color: ${location.active ? "#22543d" : "#742a2a"};
                    ">${location.active ? "Active" : "Inactive"}</span>
                </div>
                <div style="color: #4a5568; font-size: 0.875rem; line-height: 1.5;">
                    <div style="margin-bottom: 8px;"><strong>Address:</strong> ${location.address}</div>
                    <div style="margin-bottom: 8px;"><strong>Type:</strong> ${location.type}</div>
                    ${location.description ? `<div><strong>Description:</strong> ${location.description}</div>` : ""}
                </div>
            </div>
        `
    }

    bindEvents() {
        // Center button
        document.getElementById("centerBtn").addEventListener("click", () => {
            this.map.setCenter(new naver.maps.LatLng(window.mapCenter.lat, window.mapCenter.lng))
            this.map.setZoom(10)
        })

        // Refresh button
        document.getElementById("refreshBtn").addEventListener("click", () => {
            location.reload()
        })

        // Location card clicks
        document.querySelectorAll(".location-card").forEach((card) => {
            card.addEventListener("click", (e) => {
                if (e.target.closest(".action-btn")) return

                const lat = Number.parseFloat(card.dataset.lat)
                const lng = Number.parseFloat(card.dataset.lng)
                this.showOnMap(lat, lng)
            })
        })

        // Map events
        naver.maps.Event.addListener(this.map, "center_changed", () => {
            this.updateLocationDisplay()
        })
    }

    showOnMap(lat, lng) {
        const position = new naver.maps.LatLng(lat, lng)
        this.map.setCenter(position)
        this.map.setZoom(15)

        // Find and open corresponding marker
        const markerData = this.markers.find(
            (m) => Math.abs(m.location.latitude - lat) < 0.0001 && Math.abs(m.location.longitude - lng) < 0.0001,
        )

        if (markerData) {
            if (this.currentInfoWindow) {
                this.currentInfoWindow.close()
            }
            markerData.infoWindow.open(this.map, markerData.marker)
            this.currentInfoWindow = markerData.infoWindow
        }
    }

    highlightLocationCard(locationId) {
        document.querySelectorAll(".location-card").forEach((card) => {
            card.classList.remove("highlighted")
        })

        const targetCard = document.querySelector(`[data-id="${locationId}"]`)
        if (targetCard) {
            targetCard.classList.add("highlighted")
            targetCard.scrollIntoView({ behavior: "smooth", block: "center" })
        }
    }

    updateLocationDisplay() {
        const center = this.map.getCenter()
        const locationText = `${center.lat().toFixed(6)}, ${center.lng().toFixed(6)}`
        document.getElementById("currentLocation").textContent = locationText
    }

    initSearch() {
        const searchInput = document.getElementById("searchInput")
        const locationCards = document.querySelectorAll(".location-card")

        searchInput.addEventListener("input", (e) => {
            const searchTerm = e.target.value.toLowerCase()

            locationCards.forEach((card) => {
                const locationName = card.querySelector(".location-name").textContent.toLowerCase()
                const locationAddress = card.querySelector(".detail-value").textContent.toLowerCase()

                if (locationName.includes(searchTerm) || locationAddress.includes(searchTerm)) {
                    card.style.display = "block"
                } else {
                    card.style.display = "none"
                }
            })
        })
    }
}

// Global function for Thymeleaf onclick
// window.showOnMap = (lat, lng) => {
//     if (window.dashboard) {
//         window.dashboard.showOnMap(lat, lng)
//     }
// }
//
// // Initialize when DOM is loaded
// document.addEventListener("DOMContentLoaded", () => {
//     window.dashboard = new LocationDashboard()
// })
