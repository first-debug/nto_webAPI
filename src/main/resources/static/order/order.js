document.addEventListener("DOMContentLoaded", async () => {
    const params = new URLSearchParams(window.location.search);
    const orderID = params.get("orderId");
    if (!orderID) {
        console.error('Отсутствует orderId в параметрах URL');
        return;
    }

    const res = await fetch(`http://192.168.0.175:8080/api/order/${orderID}`);
    const orderInfo = await res.json();
    if (orderInfo.eventTitle === typeof undefined) {
        alert(orderInfo.message)
        window.location.href = "@/home"
        return
    }
    document.getElementById("message").innerText = "Вы успешно забронировали места!";
    const title = document.getElementById("event-title");
    title.innerText = orderInfo.eventTitle;

    const qrcodeDiv = document.getElementById("qrcode");

    new QRCode(qrcodeDiv, {
        text: `http://192.168.0.175:8080/checkOrder?orderId=${orderID}`,
        width: 256,
        height: 256,
        colorDark: "#2c3e50",
        colorLight: "#ffffff",
        correctLevel : QRCode.CorrectLevel.H
    });
    qrcodeDiv.title = "";
});