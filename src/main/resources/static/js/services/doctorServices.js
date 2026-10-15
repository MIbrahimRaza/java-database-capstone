import { API_BASE_URL } from "../config/config.js";

const DOCTOR_API = API_BASE_URL + "/doctor";

// ===== Get all doctors =====
export async function getDoctors() {
    try {
        const response = await fetch(DOCTOR_API);
        const data = await response.json();
        return data.doctors || [];
    } catch (error) {
        console.error("Error fetching doctors:", error);
        return [];
    }
}

// ===== Delete a doctor (admin only) =====
export async function deleteDoctor(id, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/delete/${id}/${token}`, {
            method: "DELETE"
        });
        const data = await response.json();
        return { success: response.ok, message: data.message };
    } catch (error) {
        console.error("Error deleting doctor:", error);
        return { success: false, message: "Something went wrong while deleting the doctor." };
    }
}

// ===== Save (add) a new doctor =====
export async function saveDoctor(doctor, token) {
    try {
        const response = await fetch(`${DOCTOR_API}/${token}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(doctor)
        });
        const data = await response.json();
        return { success: response.ok, message: data.message };
    } catch (error) {
        console.error("Error saving doctor:", error);
        return { success: false, message: "Something went wrong while saving the doctor." };
    }
}

// ===== Filter doctors by name, time and specialty =====
export async function filterDoctors(name, time, specialty) {
    try {
        // Empty filters are sent as "null" so the route segments are never empty
        const n = name && name.trim() ? encodeURIComponent(name.trim()) : "null";
        const t = time && time.trim() ? encodeURIComponent(time.trim()) : "null";
        const s = specialty && specialty.trim() ? encodeURIComponent(specialty.trim()) : "null";

        const response = await fetch(`${DOCTOR_API}/filter/${n}/${t}/${s}`);

        if (response.ok) {
            const data = await response.json();
            return data.doctors || [];
        }

        console.error("Failed to filter doctors:", response.status);
        return [];
    } catch (error) {
        console.error("Error filtering doctors:", error);
        alert("Something went wrong while filtering doctors.");
        return [];
    }
}