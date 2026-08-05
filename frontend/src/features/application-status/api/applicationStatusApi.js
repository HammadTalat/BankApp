import { httpClient } from "../../../api/httpClient";

export function getApplicationStatus() {
    return httpClient.get("/api/v1/me");
}
