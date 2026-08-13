import { setWorldConstructor } from "@cucumber/cucumber";

export class CustomWorld {
    constructor() {
        this.driver = null;
        this.testUser = null;
        this.lastAssistantMessage = "";
    }
}

setWorldConstructor(CustomWorld);
