// src/app/App.jsx
import AppRoutes from "./AppRoutes";
import { AuthProvider } from "../features/auth/context/AuthContext";


function App() {
    return <AuthProvider><AppRoutes /></AuthProvider>;
}

export default App;
