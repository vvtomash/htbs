@echo off

plantuml diagrams\*.puml -overwrite -Sdpi=300 -o ..\artifacts
cp openapi\openapi.v1.yaml artifacts\4_openapi.yaml