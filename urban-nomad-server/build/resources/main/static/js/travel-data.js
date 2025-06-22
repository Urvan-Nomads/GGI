document.addEventListener("DOMContentLoaded", function () {
    // ✅ View Image 버튼 (모달 띄우기)
    const viewButtons = document.querySelectorAll(".view-image-btn");
    const modal = document.getElementById("imageModal");
    const modalImage = document.getElementById("modalImage");
    const modalCloseBtn = document.getElementById("modalCloseBtn");

    viewButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const imageUrl = btn.dataset.img;
            if (imageUrl && imageUrl.trim() !== "") {
                modalImage.src = imageUrl;
                modal.style.display = "flex";
            } else {
                alert("이미지가 없습니다.");
            }
        });
    });

    modalCloseBtn.addEventListener("click", () => {
    modal.style.display = "none";
    modalImage.src = "";
    });

    window.addEventListener("click", (e) => {
        if (e.target === modal) {
            modal.style.display = "none";
            modalImage.src = "";
        }
    });

    // ✅ Remove 버튼 (카드 제거)
    const removeButtons = document.querySelectorAll(".remove-btn");
    removeButtons.forEach(btn => {
        btn.addEventListener("click", function () {
            const card = btn.closest(".location-card");
            if (card) {
                card.remove(); // 해당 카드 DOM에서 제거
            }
        });
    });

    const mapButtons = document.querySelectorAll(".open-navermap-btn");

    mapButtons.forEach(button => {
        button.addEventListener("click", function () {
            const address = button.dataset.address;
            if (address) {
                const encodedAddress = encodeURIComponent(address);
                const url = `https://map.naver.com/p/search/${encodedAddress}`;
                window.open(url, "_blank");
            } else {
                alert("주소 정보가 없습니다.");
            }
        });
    });
});
