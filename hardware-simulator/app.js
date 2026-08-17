import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";
import { getDatabase, ref, onValue, update } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-database.js";

const firebaseConfig = {
    apiKey: "AIzaSyCAbPGwchMaJXCdBl9xiKS4gnKcNkg8EDs",
    authDomain: "smart-home-application-29aca.firebaseapp.com",
    databaseURL: "https://smart-home-application-29aca-default-rtdb.asia-southeast1.firebasedatabase.app/",
    projectId: "smart-home-application-29aca",
    storageBucket: "smart-home-application-29aca.firebasestorage.app",
    messagingSenderId: "843437757049",
    appId: "1:843437757049:android:4e96e18c2172f172bc7aa8"
};

const app = initializeApp(firebaseConfig);
const database = getDatabase(app);
const container = document.getElementById('device-container');

let floors = null;
let devices = null;

// Connection Status Bar
const statusIndicator = document.createElement('div');
statusIndicator.className = 'status-bar';
statusIndicator.textContent = 'Connecting...';
document.body.insertBefore(statusIndicator, document.querySelector('header').nextSibling);

onValue(ref(database, '.info/connected'), (snap) => {
    if (snap.val() === true) {
        statusIndicator.textContent = '● System Online';
        statusIndicator.className = 'status-bar online';
    } else {
        statusIndicator.textContent = '○ System Offline';
        statusIndicator.className = 'status-bar offline';
    }
});

// Sync Data
onValue(ref(database, 'floors'), (snapshot) => {
    floors = snapshot.val() || {};
    render();
});

onValue(ref(database, 'devices'), (snapshot) => {
    devices = snapshot.val() || {};
    render();
});

function render() {
    if (!floors || !devices) return;
    container.innerHTML = '';

    const floorIds = Object.keys(floors);
    if (floorIds.length === 0) {
        container.innerHTML = '<div class="empty-state"><h3>No Floors Detected</h3><p>Use the Smart Home App to add your first floor.</p></div>';
        return;
    }

    floorIds.forEach(floorId => {
        const floor = floors[floorId];
        const section = document.createElement('section');
        section.className = 'floor-section';

        const title = document.createElement('h2');
        title.textContent = floor.name;
        section.appendChild(title);

        const grid = document.createElement('div');
        grid.className = 'grid-container';

        const rows = floor.grid_rows || 10;
        const cols = floor.grid_cols || 10;

        grid.style.gridTemplateColumns = `repeat(${cols}, 44px)`;
        grid.style.gridTemplateRows = `repeat(${rows}, 44px)`;

        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                const cell = document.createElement('div');
                const isDark = (r + c) % 2 !== 0;
                cell.className = `grid-cell ${isDark ? 'dark' : 'light'}`;

                const devId = Object.keys(devices).find(id =>
                    devices[id].floor_id === floorId &&
                    devices[id].grid_x === c && devices[id].grid_y === r
                );

                if (devId) {
                    const dev = devices[devId];
                    cell.appendChild(createDeviceUI(devId, dev));
                }
                grid.appendChild(cell);
            }
        }
        section.appendChild(grid);
        container.appendChild(section);
    });
}

function createDeviceUI(id, dev) {
    const el = document.createElement('div');
    el.className = `device-wrapper status-${dev.status}`;
    el.title = `${dev.name} (${dev.status})`;

    const icon = document.createElement('div');
    icon.className = 'device-icon';
    icon.textContent = getIcon(dev.type);
    el.appendChild(icon);

    if (dev.status === 'ON') {
        const glow = document.createElement('div');
        glow.className = 'glow-effect';
        el.appendChild(glow);
    }

    el.onclick = () => {
        const newStatus = dev.status === 'ON' ? 'OFF' : 'ON';
        update(ref(database, `devices/${id}`), { status: newStatus });
    };

    return el;
}

function getIcon(t) {
    const icons = {
        'ELECTRICAL_OUTLET': '🔌',
        'MULTI_SWITCH': '🎛️',
        'SAFETY_DEVICE': '🛡️',
        'SECURITY_CAMERA': '📷'
    };
    return icons[t] || '❓';
}
