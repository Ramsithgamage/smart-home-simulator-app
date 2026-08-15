import { initializeApp } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-app.js";
import { getDatabase, ref, onValue, update } from "https://www.gstatic.com/firebasejs/10.7.1/firebase-database.js";

// TODO: Replace this with your actual Firebase project configuration from the Firebase console
const firebaseConfig = {
    apiKey: "YOUR_API_KEY",
    authDomain: "YOUR_PROJECT_ID.firebaseapp.com",
    databaseURL: "https://YOUR_PROJECT_ID.firebaseio.com",
    projectId: "YOUR_PROJECT_ID",
    storageBucket: "YOUR_PROJECT_ID.appspot.com",
    messagingSenderId: "YOUR_SENDER_ID",
    appId: "YOUR_APP_ID"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const database = getDatabase(app);

const container = document.getElementById('device-container');

// Listen to changes in the "devices" node
const devicesRef = ref(database, 'devices');
onValue(devicesRef, (snapshot) => {
    container.innerHTML = '';
    const devices = snapshot.val();
    
    if (!devices) {
        container.innerHTML = '<p>No devices found.</p>';
        return;
    }

    for (const [deviceId, device] of Object.entries(devices)) {
        const card = createDeviceCard(deviceId, device);
        container.appendChild(card);
    }
});

function createDeviceCard(id, device) {
    const card = document.createElement('div');
    card.className = 'device-card';
    
    const header = document.createElement('div');
    header.className = 'device-header';
    
    const name = document.createElement('span');
    name.className = 'device-name';
    name.textContent = device.name;
    
    const status = document.createElement('span');
    status.className = `status-indicator status-${device.status}`;
    status.textContent = device.status;
    
    header.appendChild(name);
    header.appendChild(status);
    card.appendChild(header);

    if (device.type === 'MULTI_SWITCH' && device.switches) {
        const switchList = document.createElement('div');
        switchList.className = 'switch-list';
        for (const [switchId, s] of Object.entries(device.switches)) {
            const swItem = document.createElement('div');
            swItem.className = 'switch-item';
            
            const swName = document.createElement('span');
            swName.textContent = s.name;
            
            const btn = document.createElement('button');
            btn.className = `toggle-btn btn-${s.status}`;
            btn.textContent = s.status === 'ON' ? 'Turn OFF' : 'Turn ON';
            btn.onclick = () => toggleMultiSwitch(id, switchId, s.status);
            
            swItem.appendChild(swName);
            swItem.appendChild(btn);
            switchList.appendChild(swItem);
        }
        card.appendChild(switchList);
    } else {
        const btn = document.createElement('button');
        btn.className = `toggle-btn btn-${device.status}`;
        btn.textContent = device.status === 'ON' ? 'Turn OFF' : 'Turn ON';
        btn.onclick = () => toggleDevice(id, device.status);
        card.appendChild(btn);
    }

    return card;
}

function toggleDevice(deviceId, currentStatus) {
    const newStatus = currentStatus === 'ON' ? 'OFF' : 'ON';
    const updates = {};
    updates[`/devices/${deviceId}/status`] = newStatus;
    update(ref(database), updates);
}

function toggleMultiSwitch(deviceId, switchId, currentStatus) {
    const newStatus = currentStatus === 'ON' ? 'OFF' : 'ON';
    const updates = {};
    updates[`/devices/${deviceId}/switches/${switchId}/status`] = newStatus;
    update(ref(database), updates);
}
