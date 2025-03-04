document.addEventListener("DOMContentLoaded", async () => {
    const params = new URLSearchParams(window.location.search);
    const orderId = params.get("orderId");

    if (orderId == null)
        console.error("Нет параметра orderId");

    const res = await fetch(`http://localhost:8080/api/order/check/${orderId}`);
    if (res.ok) {
        document.getElementById("status-title").innerText = "Успешно!";
        document.getElementById("status-card").className = "success";
        document.getElementById("status-text").innerText = "Заказ существует!";
    } else {
        document.getElementById("status-title").innerText = "Ошибка!";
        document.getElementById("status-card").className = "error";
        document.getElementById("status-text").innerText = "Заказ не существует!";
    }
});