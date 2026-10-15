import { API_BASE_URL } from "../config/config.js";

const PATIENT_API = API_BASE_URL + "/patient";

// ===== Patient signup =====
export async function patientSignup(data) {
    try {
        const response = await fetch(PATIENT_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });
        const result = await response.json();
        return { success: response.ok, message: result.message };
    } catch (error) {
        console.error("Error during patient signup:", error);
        return { success: false, message: "Something went wrong during signup." };
    }
}

// ===== Patient login (returns the full response) =====
export async function patientLogin(data) {
    console.log("patientLogin :: ", data); // remove in production
    return await fetch(`${PATIENT_API}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });
}

// ===== Get logged-in patient data =====
export async function getPatientData(token) {
    try {
        const response = await fetch(`${PATIENT_API}/${token}`);
        const data = await response.json();
        if (response.ok) return data.patient;
        return null;
    } catch (error) {
        console.error("Error fetching patient details:", error);
        return null;
    }
}

// ===== Get appointments (shared by patient and doctor dashboards) =====
export async function getPatientAppointments(id, token, user) {
    try {
        const response = await fetch(`${PATIENT_API}/${id}/${user}/${token}`);
        const data = await response.json();
        if (response.ok) return data.appointments;
        return null;
    } catch (error) {
        console.error("Error fetching patient appointments:", error);
        return null;
    }
}

// ===== Filter appointments by condition and patient name =====
export async function filterAppointments(condition, name, token) {
    try {
        const c = condition && condition.trim() ? encodeURIComponent(condition.trim()) : "null";
        const n = name && name.trim() ? encodeURIComponent(name.trim()) : "null";

        const response = await fetch(`${PATIENT_API}/filter/${c}/${n}/${token}`);

        if (response.ok) {
            const data = await response.json();
            return data.appointments || [];
        }

        console.error("Failed to filter appointments:", response.status);
        return [];
    } catch (error) {
        console.error("Error filtering appointments:", error);
        alert("Something went wrong while filtering appointments.");
        return [];
    }
}