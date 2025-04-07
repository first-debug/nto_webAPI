document.addEventListener("DOMContentLoaded", async () => {
  try {
    const res = await fetch("http://localhost:8080/api/events");
    const events = await res.json();
    const list = document.getElementById("events-list");
    list.innerHTML = "";

    events.forEach((event) => {
      const li = document.createElement("li");
      li.innerHTML = `
                ${event.title} - ${event.startTime} 
                <button onclick="book(${event.id})">Забронировать</button>
            `;
      list.appendChild(li);
    });
  } catch (error) {
    console.error("Ошибка загрузки мероприятий:", error);
  }
});

async function book(eventId) {
  window.location.href = `/book?eventId=${eventId}`;
}
