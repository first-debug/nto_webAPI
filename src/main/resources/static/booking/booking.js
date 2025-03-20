document.addEventListener("DOMContentLoaded", async () => {
    let seats;
    let selectedSeats = Array();
    const params = new URLSearchParams(window.location.search);
    const eventId = params.get("eventId");


    if (eventId) {
        const eventRes = await fetch(`http://localhost:8080/api/events/${eventId}`);
        const event = await eventRes.json();
        document.getElementById("event-title").innerText = event.title;
        document.getElementById("event-date").innerText = event.startTime;

        const seatsRes = await fetch(`http://localhost:8080/api/events/${event.id}/seats`);
        const seatsJson = await seatsRes.json();
        seats = seatsJson.seats;

        const seatsGrid = document.querySelector('.seats-grid');

        const rows = seats.length;
        const cols = seats[0].length;

        seatsGrid.style.gridTemplateColumns = `repeat(${cols}, 30px)`;

        for (let i = 0, count = 0, row = 1; i < rows; i++) {
            for (let j = 0, col = 1; j < cols; j++) {
                const seat = document.createElement('div');
                seat.dataset.row = i;
                seat.dataset.col = j;
                seat.dataset.number = count++;
                seat.className = 'seat';
                if (seats[i][j] === 1) {
                    seat.innerHTML = `<div class="tooltip">Ряд ${row} Место ${col}</div>`
                    seat.onclick = async () => {
                        if (seats[seat.dataset.row][seat.dataset.col] === 1) {
                            seats[seat.dataset.row][seat.dataset.col] = 2;
                            seat.className = seat.className.replace("exist", "selected");
                            selectedSeats.push(Array(seat.dataset.row, seat.dataset.col, seat.dataset.number));
                        } else {
                            seats[seat.dataset.row][seat.dataset.col] = 1;
                            seat.className = seat.className.replace("selected", "exist");
                            selectedSeats = selectedSeats.filter((i) =>
                                i[0] !== seat.dataset.row || i[1] !== seat.dataset.col);
                        }
                    }
                    col++;
                    seat.className += " exist";
                } else if (seats[i][j] === 2) {
                    seat.className += " occupied";
                    col++;
                } else if (seats[i][j] === 3) {
                    seat.className += " come";
                    col++;
                }
                seatsGrid.appendChild(seat);
            }
            row++;
        }

        const footer = document.getElementById("footer");
        const button = document.createElement("button");
        button.textContent = "Забронировать";
        button.className = "book-button";
        button.addEventListener("click", async () => {
            try {
                const res = await fetch(`http://localhost:8080/api/events/book`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ eventId, seats: selectedSeats })
                });
                const data = await res.json();
                if (data.orderId === typeof undefined) {
                    alert(data.message);
                    window.location.reload()
                } else {
                    window.location.href = `/order?orderId=${data.orderId}`;
                }
            } catch (error) {
                console.error("Ошибка бронирования:", error);
                alert("Ошибка при бронировании");
            }
        });
        footer.appendChild(button);
    } else {
        document.getElementById("event-title").innerText = `Event with ID: ${eventId} not found`;
        console.error(`Event with ID: ${eventId} not found`)

    }
});
