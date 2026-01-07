document.addEventListener('DOMContentLoaded', function() {
    const searchInput = document.getElementById('eventSearch');

    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const query = e.target.value.toLowerCase();
            const cards = document.querySelectorAll('.event-card');

            cards.forEach(card => {
                const title = card.querySelector('h3').textContent.toLowerCase();
                if (title.includes(query)) {
                    card.parentElement.style.display = 'block';
                } else {
                    card.parentElement.style.display = 'none';
                }
            });
        });
    }
});