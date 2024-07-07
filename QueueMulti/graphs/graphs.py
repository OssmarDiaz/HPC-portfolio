import pandas as pd
import plotly.express as px

# Leer los archivos CSV
df1 = pd.read_csv('tiemposEnqDeq.csv')
df2 = pd.read_csv('tiemposEnqDeqM.csv')

# Tomar las dos primeras columnas de cada archivo
x1 = df1.iloc[:, 0]
y1 = df1.iloc[:, 1]
x2 = df2.iloc[:, 0]
y2 = df2.iloc[:, 1]

# Crear la figura
fig = px.line()

# Agregar los datos del primer archivo a la figura
fig.add_scatter(x=x1, y=y1, mode='lines', name='LockFreeQueue')

# Agregar los datos del segundo archivo a la figura
fig.add_scatter(x=x2, y=y2, mode='lines', name='LockFreeQueueMulti')

# Actualizar el layout de la figura
fig.update_layout(
    title='Comparación de Tiempos de Enqueue/Dequeue',
    xaxis_title='Tiempo',
    yaxis_title='Valor',
    legend_title='Leyenda'
)

# Mostrar la figura
fig.show()