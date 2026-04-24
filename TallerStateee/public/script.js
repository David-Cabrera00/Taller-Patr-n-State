const messageBox = document.getElementById("messageBox");

window.addEventListener("load", loadAccount);

async function loadAccount() {
    try {
        const response = await fetch("/api/account");
        const data = await response.json();
        renderData(data);
    } catch (error) {
        showMessage("No se pudo conectar con el servidor Java.", true);
    }
}

async function createAccount() {
    const ownerName = document.getElementById("ownerName").value.trim();
    const initialBalance = Number(document.getElementById("initialBalance").value);
    const overdraftLimit = Number(document.getElementById("overdraftLimit").value);

    if (!ownerName) {
        showMessage("Debe ingresar el nombre del titular.", true);
        return;
    }

    if (ownerName.length > 40) {
        showMessage("El nombre del titular no puede superar 40 caracteres.", true);
        return;
    }

    if (initialBalance < 0 || overdraftLimit < 0) {
        showMessage("Los valores no pueden ser negativos.", true);
        return;
    }

    await sendRequest("/api/account/create", {
        ownerName,
        initialBalance,
        overdraftLimit
    });
}

async function deposit() {
    await sendMoneyOperation("/api/account/deposit");
}

async function withdraw() {
    await sendMoneyOperation("/api/account/withdraw");
}

async function transfer() {
    await sendMoneyOperation("/api/account/transfer");
}

async function freeze() {
    await sendRequest("/api/account/freeze", {});
}

async function unfreeze() {
    await sendRequest("/api/account/unfreeze", {});
}

async function closeAccount() {
    const confirmed = confirm("¿Está seguro de intentar cerrar la cuenta? El saldo debe estar en cero.");

    if (!confirmed) {
        return;
    }

    await sendRequest("/api/account/close", {});
}

async function sendMoneyOperation(url) {
    const amount = Number(document.getElementById("amount").value);

    if (!amount || amount <= 0) {
        showMessage("Debe ingresar un monto mayor que cero.", true);
        return;
    }

    await sendRequest(url, { amount });
}

async function sendRequest(url, body) {
    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });

        const data = await response.json();

        if (!data.success) {
            showMessage(data.message, true);
            return;
        }

        renderData(data);
    } catch (error) {
        showMessage("Ocurrió un error al comunicarse con el servidor.", true);
    }
}

function renderData(data) {
    if (!data.account) {
        document.getElementById("accountNumber").textContent = "-";
        document.getElementById("accountOwner").textContent = "-";
        document.getElementById("balance").textContent = "-";
        document.getElementById("overdraft").textContent = "-";
        document.getElementById("stateName").textContent = "Sin cuenta";
        document.getElementById("stateDescription").textContent = "Cree una cuenta para iniciar.";
        document.getElementById("history").textContent = "No hay transacciones para mostrar.";

        if (data.targetAccount) {
            document.getElementById("targetBalance").textContent = data.targetAccount.balance;
        }

        updateButtons(null);
        showMessage(data.message || "Cree una cuenta para iniciar el taller.", false);
        return;
    }

    document.getElementById("accountNumber").textContent = data.account.accountNumber;
    document.getElementById("accountOwner").textContent = data.account.ownerName;
    document.getElementById("balance").textContent = data.account.balance;
    document.getElementById("overdraft").textContent = data.account.overdraftLimit;
    document.getElementById("stateName").textContent = data.account.stateName;
    document.getElementById("stateDescription").textContent = getStateDescription(data.account.stateName);

    if (data.targetAccount) {
        document.getElementById("targetBalance").textContent = data.targetAccount.balance;
    }

    renderHistory(data.account.transactions);
    updateButtons(data.account.stateName);
    showMessage(data.message, false);
}

function renderHistory(transactions) {
    const history = document.getElementById("history");

    if (!transactions || transactions.length === 0) {
        history.textContent = "No hay transacciones para mostrar.";
        return;
    }

    history.innerHTML = "";

    transactions.slice().reverse().forEach(transaction => {
        const item = document.createElement("div");
        item.className = "history-item " + getTransactionClass(transaction);
        item.textContent = transaction;
        history.appendChild(item);
    });
}

function getTransactionClass(transaction) {
    if (transaction.includes("RECHAZADO")) {
        return "rejected";
    }

    if (transaction.includes("DEPÓSITO")) {
        return "deposit";
    }

    if (transaction.includes("RETIRO")) {
        return "withdraw";
    }

    if (transaction.includes("TRANSFERENCIA")) {
        return "transfer";
    }

    if (transaction.includes("CAMBIO DE ESTADO")) {
        return "state-change";
    }

    return "";
}

function updateButtons(stateName) {
    const hasAccount = stateName !== null;

    const depositButton = document.getElementById("depositButton");
    const withdrawButton = document.getElementById("withdrawButton");
    const transferButton = document.getElementById("transferButton");
    const freezeButton = document.getElementById("freezeButton");
    const unfreezeButton = document.getElementById("unfreezeButton");
    const closeButton = document.getElementById("closeButton");

    depositButton.disabled = !hasAccount || stateName === "Cerrada";
    withdrawButton.disabled = !hasAccount || stateName !== "Activa";
    transferButton.disabled = !hasAccount || stateName !== "Activa";
    freezeButton.disabled = !hasAccount || stateName === "Bloqueada" || stateName === "Cerrada";
    unfreezeButton.disabled = !hasAccount || stateName !== "Bloqueada";
    closeButton.disabled = !hasAccount || stateName === "Cerrada";
}

function getStateDescription(stateName) {
    switch (stateName) {
        case "Activa":
            return "La cuenta puede operar normalmente.";
        case "Sobregirada":
            return "La cuenta debe recibir dinero para pagar el saldo negativo.";
        case "Bloqueada":
            return "La cuenta está restringida temporalmente.";
        case "Cerrada":
            return "La cuenta ya no permite operaciones.";
        default:
            return "Estado no reconocido.";
    }
}

function showMessage(message, isError) {
    messageBox.textContent = message;
    messageBox.classList.toggle("error", isError);
}
function startSimulation() {
    const simulationSection = document.getElementById("simulationSection");
    simulationSection.classList.remove("hidden");
    simulationSection.scrollIntoView({ behavior: "smooth", block: "start" });
}

function scrollToContext() {
    document.getElementById("contextSection").scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}